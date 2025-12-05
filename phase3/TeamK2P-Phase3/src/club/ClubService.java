package club;

import db.DBUtil;

import java.sql.*;
import java.util.Scanner;

public class ClubService {

    // 1. 전체 클럽 목록
    public void listAllClubs() {
        String sql =
                "SELECT c.club_id, c.name, c.club_type, c.status, cat.name AS category_name " +
                "FROM clubs c " +
                "LEFT JOIN categories cat ON c.category_id = cat.category_id " +
                "ORDER BY c.club_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n===== 전체 클럽 목록 =====");
            while (rs.next()) {
                int clubId = rs.getInt("club_id");
                String name = rs.getString("name");
                String type = rs.getString("club_type");
                String status = rs.getString("status");
                String categoryName = rs.getString("category_name");

                System.out.printf(
                        "[ID:%d] %s | 유형:%s | 카테고리:%s | 상태:%s%n",
                        clubId,
                        name,
                        type,
                        (categoryName == null ? "-" : categoryName),
                        status
                );
            }

        } catch (SQLException e) {
            System.out.println("[에러] 전체 클럽 목록 조회 실패");
            e.printStackTrace();
        }
    }

    // 2. 카테고리명으로 클럽 검색
    public void listClubsByCategory(Scanner sc) {
        System.out.print("카테고리 이름 키워드 (예: Category, Sports 등) >> ");
        String keyword = sc.nextLine().trim();

        String sql =
                "SELECT c.club_id, c.name, c.club_type, c.status, cat.name AS category_name " +
                "FROM clubs c " +
                "LEFT JOIN categories cat ON c.category_id = cat.category_id " +
                "WHERE LOWER(cat.name) LIKE LOWER(?) " +
                "ORDER BY c.club_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n===== 카테고리 검색 결과 =====");
                boolean hasRow = false;
                while (rs.next()) {
                    hasRow = true;
                    int clubId = rs.getInt("club_id");
                    String name = rs.getString("name");
                    String type = rs.getString("club_type");
                    String status = rs.getString("status");
                    String categoryName = rs.getString("category_name");

                    System.out.printf(
                            "[ID:%d] %s | 유형:%s | 카테고리:%s | 상태:%s%n",
                            clubId,
                            name,
                            type,
                            (categoryName == null ? "-" : categoryName),
                            status
                    );
                }

                if (!hasRow) {
                    System.out.println("해당 카테고리에 해당하는 클럽이 없습니다.");
                }
            }

        } catch (SQLException e) {
            System.out.println("[에러] 카테고리별 클럽 검색 실패");
            e.printStackTrace();
        }
    }

    // 3. 이름/키워드로 클럽 검색
    public void searchClubsByName(Scanner sc) {
        System.out.print("클럽 이름 키워드 >> ");
        String keyword = sc.nextLine().trim();

        String sql =
                "SELECT club_id, name, club_type, status " +
                "FROM clubs " +
                "WHERE LOWER(name) LIKE LOWER(?) " +
                "ORDER BY club_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n===== 이름 검색 결과 =====");
                boolean hasRow = false;
                while (rs.next()) {
                    hasRow = true;
                    int clubId = rs.getInt("club_id");
                    String name = rs.getString("name");
                    String type = rs.getString("club_type");
                    String status = rs.getString("status");

                    System.out.printf(
                            "[ID:%d] %s | 유형:%s | 상태:%s%n",
                            clubId,
                            name,
                            type,
                            status
                    );
                }
                if (!hasRow) {
                    System.out.println("해당 이름의 클럽이 없습니다.");
                }
            }

        } catch (SQLException e) {
            System.out.println("[에러] 이름으로 클럽 검색 실패");
            e.printStackTrace();
        }
    }

    // 4. 내가 가입한 클럽 목록
    public void listMyClubs(int userId) {
        String sql =
                "SELECT m.club_id, m.role, m.status, " +
                "       c.name, c.club_type " +
                "FROM memberships m " +
                "JOIN clubs c ON m.club_id = c.club_id " +
                "WHERE m.user_id = ? " +
                "ORDER BY c.club_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n===== 내가 가입한 클럽 =====");
                boolean hasRow = false;
                while (rs.next()) {
                    hasRow = true;
                    int clubId = rs.getInt("club_id");
                    String name = rs.getString("name");
                    String type = rs.getString("club_type");
                    String role = rs.getString("role");
                    String status = rs.getString("status");

                    System.out.printf(
                            "[ID:%d] %s | 유형:%s | 내 역할:%s | 상태:%s%n",
                            clubId, name, type, role, status
                    );
                }
                if (!hasRow) {
                    System.out.println("가입한 클럽이 없습니다.");
                }
            }

        } catch (SQLException e) {
            System.out.println("[에러] 내 클럽 목록 조회 실패");
            e.printStackTrace();
        }
    }

    // 5. 클럽 가입
    public void joinClub(Scanner sc, int userId) {
        System.out.print("가입할 클럽 ID >> ");
        String input = sc.nextLine().trim();
        int clubId;
        try {
            clubId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("클럽 ID는 숫자로 입력해야 합니다.");
            return;
        }

        String sql =
                "INSERT INTO memberships(user_id, club_id, role, joined_at, left_at, status) " +
                "VALUES (?, ?, 'MEMBER', SYSDATE, NULL, 'ACTIVE')";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, clubId);

            int rows = ps.executeUpdate();
            System.out.println("[클럽 가입 완료] (" + rows + "건)");

        } catch (SQLIntegrityConstraintViolationException e) {
            // UNIQUE(user_id, club_id) 위반 또는 FK 실패
            System.out.println("[가입 실패] 이미 가입했거나 존재하지 않는 클럽입니다.");
        } catch (SQLException e) {
            System.out.println("[에러] 클럽 가입 중 오류");
            e.printStackTrace();
        }
    }

    // 6. 클럽 탈퇴 (상태만 변경)
    public void leaveClub(Scanner sc, int userId) {
        System.out.print("탈퇴할 클럽 ID >> ");
        String input = sc.nextLine().trim();
        int clubId;
        try {
            clubId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("클럽 ID는 숫자로 입력해야 합니다.");
            return;
        }

        String sql =
                "UPDATE memberships " +
                "SET status = 'LEFT', left_at = SYSDATE " +
                "WHERE user_id = ? AND club_id = ? AND status = 'ACTIVE'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, clubId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[클럽 탈퇴 완료]");
            } else {
                System.out.println("해당 클럽에 ACTIVE 상태로 가입된 이력이 없습니다.");
            }

        } catch (SQLException e) {
            System.out.println("[에러] 클럽 탈퇴 중 오류");
            e.printStackTrace();
        }
    }
}
