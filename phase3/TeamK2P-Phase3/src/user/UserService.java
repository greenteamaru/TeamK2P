package user;

import db.DBUtil;

import java.sql.*;
import java.util.Scanner;

public class UserService {

    // 회원가입
    public void signUp(Scanner sc) {
        System.out.print("이름: ");
        String name = sc.nextLine();

        System.out.print("이메일(ID): ");
        String email = sc.nextLine();

        System.out.print("비밀번호: ");
        String password = sc.nextLine();

        System.out.print("국적(없으면 엔터): ");
        String nationality = sc.nextLine();

        System.out.print("닉네임(없으면 엔터): ");
        String nickname = sc.nextLine();

        System.out.print("언어(예: ko, en): ");
        String language = sc.nextLine();

        String sql = """
            INSERT INTO users (name, email, password, nationality, nickname, language, created_at, is_admin)
            VALUES (?, ?, ?, ?, ?, ?, SYSDATE, 'N')
            """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, nationality.isBlank() ? null : nationality);
            ps.setString(5, nickname.isBlank() ? null : nickname);
            ps.setString(6, language.isBlank() ? null : language);

            int rows = ps.executeUpdate();
            System.out.println("[회원가입 완료] (" + rows + "명)");

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("[회원가입 실패] 이미 존재하는 이메일입니다.");
        } catch (SQLException e) {
            System.out.println("[회원가입 실패] DB 오류");
            e.printStackTrace();
        }
    }

    // 로그인 : 성공 시 user_id 반환, 실패 시 null
    public Integer login(Scanner sc) {
        System.out.print("이메일(ID): ");
        String email = sc.nextLine();

        System.out.print("비밀번호: ");
        String password = sc.nextLine();

        String sql = "SELECT user_id FROM users WHERE email = ? AND password = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    return userId;
                } else {
                    System.out.println("[로그인 실패] 이메일 또는 비밀번호가 올바르지 않습니다.");
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("[로그인 실패] DB 오류");
            e.printStackTrace();
            return null;
        }
    }

    // 관리자 여부 확인
    public boolean isAdmin(int userId) {
        String sql = "SELECT is_admin FROM users WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "Y".equals(rs.getString("is_admin"));
                }
            }
        } catch (SQLException e) {
            System.out.println("[에러] 관리자 여부 확인 실패");
            e.printStackTrace();
        }
        return false;
    }

    // 내 정보 조회
    public void viewProfile(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("===== 내 정보 =====");
                    System.out.println("ID: " + rs.getInt("user_id"));
                    System.out.println("이름: " + rs.getString("name"));
                    System.out.println("이메일: " + rs.getString("email"));
                    System.out.println("국적: " + rs.getString("nationality"));
                    System.out.println("닉네임: " + rs.getString("nickname"));
                    System.out.println("언어: " + rs.getString("language"));
                    System.out.println("관리자 여부: " + rs.getString("is_admin"));
                    System.out.println("가입일: " + rs.getDate("created_at"));
                } else {
                    System.out.println("사용자를 찾을 수 없습니다.");
                }
            }
        } catch (SQLException e) {
            System.out.println("[에러] 내 정보 조회 실패");
            e.printStackTrace();
        }
    }

    // 내 정보 수정 (email은 수정 안 함)
    public void updateProfile(Scanner sc, int userId) {
        System.out.println("수정할 항목 선택:");
        System.out.println("1. 이름");
        System.out.println("2. 국적");
        System.out.println("3. 닉네임");
        System.out.println("4. 언어");
        System.out.print("선택 >> ");

        String choice = sc.nextLine();
        String column;

        switch (choice) {
            case "1" -> column = "name";
            case "2" -> column = "nationality";
            case "3" -> column = "nickname";
            case "4" -> column = "language";
            default -> {
                System.out.println("잘못된 선택입니다.");
                return;
            }
        }

        System.out.print("새 값 입력 >> ");
        String value = sc.nextLine();

        String sql = "UPDATE users SET " + column + " = ? WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, value.isBlank() ? null : value);
            ps.setInt(2, userId);

            int rows = ps.executeUpdate();
            System.out.println("[정보 수정 완료] (" + rows + "건)");

        } catch (SQLException e) {
            System.out.println("[에러] 내 정보 수정 실패");
            e.printStackTrace();
        }
    }

    // 비밀번호 변경
    public void changePassword(Scanner sc, int userId) {
        System.out.print("새 비밀번호: ");
        String newPw = sc.nextLine();

        String sql = "UPDATE users SET password = ? WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPw);
            ps.setInt(2, userId);

            int rows = ps.executeUpdate();
            System.out.println("[비밀번호 변경 완료] (" + rows + "건)");

        } catch (SQLException e) {
            System.out.println("[에러] 비밀번호 변경 실패");
            e.printStackTrace();
        }
    }

    // 회원 탈퇴
    public void deleteAccount(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            int rows = ps.executeUpdate();
            System.out.println("[회원 탈퇴 완료] (" + rows + "건)");

        } catch (SQLException e) {
            System.out.println("[에러] 회원 탈퇴 실패");
            e.printStackTrace();
        }
    }

    // 관리자: 전체 회원 조회
    public void viewAllUsers() {
        String sql = "SELECT user_id, name, email, is_admin FROM users ORDER BY user_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("===== 전체 회원 목록 =====");
            while (rs.next()) {
                System.out.printf("%d | %s | %s | 관리자:%s%n",
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("is_admin"));
            }

        } catch (SQLException e) {
            System.out.println("[에러] 전체 회원 조회 실패");
            e.printStackTrace();
        }
    }

    // 관리자: 이메일로 회원 검색
    public void findUserByEmail(Scanner sc) {
        System.out.print("검색할 이메일 키워드 >> ");
        String keyword = sc.nextLine();

        String sql = "SELECT user_id, name, email FROM users WHERE email LIKE ? ORDER BY user_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("===== 검색 결과 =====");
                boolean hasRow = false;
                while (rs.next()) {
                    hasRow = true;
                    System.out.printf("%d | %s | %s%n",
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("email"));
                }
                if (!hasRow) {
                    System.out.println("검색 결과가 없습니다.");
                }
            }

        } catch (SQLException e) {
            System.out.println("[에러] 회원 검색 실패");
            e.printStackTrace();
        }
    }
}
