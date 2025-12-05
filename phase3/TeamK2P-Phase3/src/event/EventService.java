package event;

import db.DBUtil;

import java.sql.*;
import java.util.Scanner;

public class EventService {

    // 1. 다가오는 이벤트 20개 조회
    public void listUpcomingEvents() {
        String sql =
                "SELECT e.event_id, e.title, e.start_time, e.end_time, " +
                "       v.campus, v.building, v.room, c.name AS club_name " +
                "FROM events e " +
                "JOIN venues v ON e.venue_id = v.venue_id " +
                "JOIN clubs c  ON e.club_id  = c.club_id " +
                "WHERE e.start_time >= SYSDATE " +
                "ORDER BY e.start_time ASC " +
                "FETCH FIRST 20 ROWS ONLY";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n===== 다가오는 이벤트 (최대 20개) =====");
            while (rs.next()) {
                int eventId = rs.getInt("event_id");
                String title = rs.getString("title");
                Timestamp start = rs.getTimestamp("start_time");
                Timestamp end = rs.getTimestamp("end_time");
                String campus = rs.getString("campus");
                String building = rs.getString("building");
                String room = rs.getString("room");
                String clubName = rs.getString("club_name");

                System.out.printf(
                        "[ID:%d] %s | %s ~ %s | %s (%s %s, %s)\n",
                        eventId,
                        title,
                        start, end,
                        clubName,
                        campus, building, room
                );
            }

        } catch (SQLException e) {
            System.out.println("[에러] 이벤트 목록 조회 중 오류 발생");
            e.printStackTrace();
        }
    }

    // 2. 캠퍼스별 이벤트 조회
    public void listEventsByCampus(Scanner sc) {
        System.out.print("캠퍼스 입력 (예: KNU, Med, Eng) >> ");
        String campus = sc.nextLine().trim();

        String sql =
                "SELECT e.event_id, e.title, e.start_time, v.building, v.room, c.name AS club_name " +
                "FROM events e " +
                "JOIN venues v ON e.venue_id = v.venue_id " +
                "JOIN clubs c  ON e.club_id  = c.club_id " +
                "WHERE v.campus = ? " +
                "ORDER BY e.start_time";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, campus);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n===== " + campus + " 캠퍼스 이벤트 =====");
                boolean hasRow = false;
                while (rs.next()) {
                    hasRow = true;
                    int eventId = rs.getInt("event_id");
                    String title = rs.getString("title");
                    Timestamp start = rs.getTimestamp("start_time");
                    String building = rs.getString("building");
                    String room = rs.getString("room");
                    String clubName = rs.getString("club_name");

                    System.out.printf(
                            "[ID:%d] %s | %s | %s (%s)\n",
                            eventId, title, start, building, room, clubName
                    );
                }
                if (!hasRow) {
                    System.out.println("해당 캠퍼스의 이벤트가 없습니다.");
                }
            }

        } catch (SQLException e) {
            System.out.println("[에러] 캠퍼스별 이벤트 조회 중 오류");
            e.printStackTrace();
        }
    }

    // 3. 이벤트 상세보기 + 간단 리뷰/평점
    public void viewEventDetail(Scanner sc) {
        System.out.print("상세 조회할 이벤트 ID >> ");
        String input = sc.nextLine().trim();
        int eventId;
        try {
            eventId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("이벤트 ID는 숫자로 입력해야 합니다.");
            return;
        }

        String sqlEvent =
                "SELECT e.event_id, e.title, e.summary, e.start_time, e.end_time, " +
                "       e.language, e.capacity, e.fee, e.status, " +
                "       v.campus, v.building, v.room, " +
                "       c.name AS club_name " +
                "FROM events e " +
                "JOIN venues v ON e.venue_id = v.venue_id " +
                "JOIN clubs c  ON e.club_id  = c.club_id " +
                "WHERE e.event_id = ?";

        String sqlReview =
                "SELECT COUNT(*) AS review_cnt, AVG(rating) AS avg_rating " +
                "FROM reviews " +
                "WHERE event_id = ? AND status = 'VISIBLE'";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement psEvent = conn.prepareStatement(sqlEvent);
             PreparedStatement psReview = conn.prepareStatement(sqlReview)) {

            // 이벤트 기본 정보
            psEvent.setInt(1, eventId);
            try (ResultSet rs = psEvent.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("해당 ID의 이벤트가 존재하지 않습니다.");
                    return;
                }

                System.out.println("\n===== 이벤트 상세 정보 =====");
                System.out.println("ID        : " + rs.getInt("event_id"));
                System.out.println("제목      : " + rs.getString("title"));
                System.out.println("요약      : " + rs.getString("summary"));
                System.out.println("시간      : " + rs.getTimestamp("start_time")
                        + " ~ " + rs.getTimestamp("end_time"));
                System.out.println("언어      : " + rs.getString("language"));
                System.out.println("정원      : " + rs.getInt("capacity"));
                System.out.println("참가비    : " + rs.getInt("fee"));
                System.out.println("상태      : " + rs.getString("status"));
                System.out.println("장소      : " +
                        rs.getString("campus") + " " +
                        rs.getString("building") + " " +
                        rs.getString("room"));
                System.out.println("주최 클럽 : " + rs.getString("club_name"));
            }

            // 리뷰 요약
            psReview.setInt(1, eventId);
            try (ResultSet rs2 = psReview.executeQuery()) {
                if (rs2.next()) {
                    int reviewCnt = rs2.getInt("review_cnt");
                    double avgRating = rs2.getDouble("avg_rating");
                    System.out.println("리뷰 수   : " + reviewCnt);
                    if (reviewCnt > 0) {
                        System.out.printf("평균 평점 : %.2f / 5.0\n", avgRating);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("[에러] 이벤트 상세 조회 중 오류");
            e.printStackTrace();
        }
    }

    // 4. 내 신청 목록 확인
    public void listMyRegistrations(int userId) {
        String sql =
                "SELECT r.event_id, e.title, e.start_time, e.end_time, " +
                "       r.status, r.attended " +
                "FROM registrations r " +
                "JOIN events e ON r.event_id = e.event_id " +
                "WHERE r.user_id = ? " +
                "ORDER BY e.start_time DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n===== 내 이벤트 신청 목록 =====");
                boolean hasRow = false;
                while (rs.next()) {
                    hasRow = true;
                    int eventId = rs.getInt("event_id");
                    String title = rs.getString("title");
                    Timestamp start = rs.getTimestamp("start_time");
                    Timestamp end = rs.getTimestamp("end_time");
                    String status = rs.getString("status");
                    String attended = rs.getString("attended");

                    System.out.printf(
                            "[ID:%d] %s | %s ~ %s | 상태:%s | 참석:%s\n",
                            eventId, title, start, end, status, attended
                    );
                }
                if (!hasRow) {
                    System.out.println("신청한 이벤트가 없습니다.");
                }
            }

        } catch (SQLException e) {
            System.out.println("[에러] 내 신청 목록 조회 오류");
            e.printStackTrace();
        }
    }

    // 5. 이벤트 신청
    public void registerEvent(Scanner sc, int userId) {
        System.out.print("신청할 이벤트 ID >> ");
        String input = sc.nextLine().trim();
        int eventId;
        try {
            eventId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("이벤트 ID는 숫자로 입력해야 합니다.");
            return;
        }

        String sql =
                "INSERT INTO registrations(user_id, event_id, status, attended, registered_at) " +
                "VALUES (?, ?, 'CONFIRMED', 'N', SYSDATE)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, eventId);

            int rows = ps.executeUpdate();
            System.out.println("[신청 완료] (" + rows + "건)");

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("[신청 실패] 이미 신청했거나 존재하지 않는 이벤트입니다.");
        } catch (SQLException e) {
            System.out.println("[에러] 이벤트 신청 중 오류");
            e.printStackTrace();
        }
    }

    // 6. 이벤트 신청 취소
    public void cancelRegistration(Scanner sc, int userId) {
        System.out.print("취소할 이벤트 ID >> ");
        String input = sc.nextLine().trim();
        int eventId;
        try {
            eventId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("이벤트 ID는 숫자로 입력해야 합니다.");
            return;
        }

        String sql =
                "UPDATE registrations " +
                "SET status = 'CANCELLED' " +
                "WHERE user_id = ? AND event_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, eventId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[신청 취소 완료]");
            } else {
                System.out.println("해당 이벤트에 대한 신청 내역이 없습니다.");
            }

        } catch (SQLException e) {
            System.out.println("[에러] 신청 취소 중 오류");
            e.printStackTrace();
        }
    }
}
