package review;

import db.DBUtil;

import java.sql.*;
import java.util.Scanner;

public class ReviewService {

    // 1. 내가 쓴 리뷰 목록
    public void listMyReviews(int userId) {
        String sql =
            "SELECT r.review_id, r.event_id, e.title, r.rating, r.review_text, " +
            "       r.created_at, r.status " +
            "FROM reviews r " +
            "JOIN events e ON r.event_id = e.event_id " +
            "WHERE r.user_id = ? " +
            "ORDER BY r.created_at DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n===== 내가 쓴 리뷰 목록 =====");
                boolean hasRow = false;
                while (rs.next()) {
                    hasRow = true;
                    int reviewId = rs.getInt("review_id");
                    int eventId  = rs.getInt("event_id");
                    String title = rs.getString("title");
                    int rating   = rs.getInt("rating");
                    String text  = rs.getString("review_text");
                    Timestamp created = rs.getTimestamp("created_at");
                    String status = rs.getString("status");

                    System.out.printf(
                        "[리뷰ID:%d] 이벤트ID:%d | %s | 평점:%d | 상태:%s\n내용: %s\n작성일: %s\n\n",
                        reviewId, eventId, title, rating, status,
                        text, created
                    );
                }
                if (!hasRow) {
                    System.out.println("작성한 리뷰가 없습니다.");
                }
            }

        } catch (SQLException e) {
            System.out.println("[에러] 내 리뷰 목록 조회 중 오류");
            e.printStackTrace();
        }
    }

    // 2. 특정 이벤트에 달린 리뷰 보기
    public void listReviewsByEvent(Scanner sc) {
        System.out.print("리뷰를 볼 이벤트 ID >> ");
        String input = sc.nextLine().trim();
        int eventId;
        try {
            eventId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("이벤트 ID는 숫자로 입력해야 합니다.");
            return;
        }

        String sql =
            "SELECT r.review_id, u.nickname, u.name, r.rating, r.review_text, r.created_at " +
            "FROM reviews r " +
            "JOIN users u ON r.user_id = u.user_id " +
            "WHERE r.event_id = ? AND r.status = 'VISIBLE' " +
            "ORDER BY r.created_at DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n===== 이벤트 " + eventId + " 리뷰 목록 =====");
                boolean hasRow = false;
                while (rs.next()) {
                    hasRow = true;
                    int reviewId  = rs.getInt("review_id");
                    String nick   = rs.getString("nickname");
                    String name   = rs.getString("name");
                    int rating    = rs.getInt("rating");
                    String text   = rs.getString("review_text");
                    Timestamp created = rs.getTimestamp("created_at");

                    System.out.printf(
                        "[리뷰ID:%d] 작성자:%s(%s) | 평점:%d\n내용: %s\n작성일: %s\n\n",
                        reviewId, nick, name, rating, text, created
                    );
                }
                if (!hasRow) {
                    System.out.println("해당 이벤트에 등록된 리뷰가 없습니다.");
                }
            }

        } catch (SQLException e) {
            System.out.println("[에러] 이벤트 리뷰 조회 중 오류");
            e.printStackTrace();
        }
    }

    // 3. 리뷰 작성
    public void writeReview(Scanner sc, int userId) {
        System.out.print("리뷰 작성할 이벤트 ID >> ");
        String inputEvent = sc.nextLine().trim();
        int eventId;
        try {
            eventId = Integer.parseInt(inputEvent);
        } catch (NumberFormatException e) {
            System.out.println("이벤트 ID는 숫자로 입력해야 합니다.");
            return;
        }

        System.out.print("평점 (1~5) >> ");
        String inputRating = sc.nextLine().trim();
        int rating;
        try {
            rating = Integer.parseInt(inputRating);
            if (rating < 1 || rating > 5) {
                System.out.println("평점은 1~5 사이여야 합니다.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("평점은 숫자로 입력해야 합니다.");
            return;
        }

        System.out.print("리뷰 내용 >> ");
        String text = sc.nextLine();

        String sql =
            "INSERT INTO reviews(user_id, event_id, rating, review_text, status, created_at) " +
            "VALUES (?, ?, ?, ?, 'VISIBLE', SYSDATE)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            ps.setInt(3, rating);
            ps.setString(4, text);

            int rows = ps.executeUpdate();
            System.out.println("[리뷰 등록 완료] (" + rows + "건)");

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("[리뷰 등록 실패] 존재하지 않는 이벤트이거나 FK 제약 위반입니다.");
        } catch (SQLException e) {
            System.out.println("[에러] 리뷰 등록 중 오류");
            e.printStackTrace();
        }
    }

    // 4. 리뷰 수정
    public void updateMyReview(Scanner sc, int userId) {
        System.out.print("수정할 리뷰 ID >> ");
        String input = sc.nextLine().trim();
        int reviewId;
        try {
            reviewId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("리뷰 ID는 숫자로 입력해야 합니다.");
            return;
        }

        System.out.print("새 평점 (1~5) >> ");
        String inputRating = sc.nextLine().trim();
        int rating;
        try {
            rating = Integer.parseInt(inputRating);
            if (rating < 1 || rating > 5) {
                System.out.println("평점은 1~5 사이여야 합니다.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("평점은 숫자로 입력해야 합니다.");
            return;
        }

        System.out.print("새 리뷰 내용 >> ");
        String text = sc.nextLine();

        String sql =
            "UPDATE reviews " +
            "SET rating = ?, review_text = ? " +
            "WHERE review_id = ? AND user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, rating);
            ps.setString(2, text);
            ps.setInt(3, reviewId);
            ps.setInt(4, userId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[리뷰 수정 완료]");
            } else {
                System.out.println("해당 리뷰가 없거나 본인 리뷰가 아닙니다.");
            }

        } catch (SQLException e) {
            System.out.println("[에러] 리뷰 수정 중 오류");
            e.printStackTrace();
        }
    }

    // 5. 리뷰 삭제 (DB에서 실제 삭제)
    public void deleteMyReview(Scanner sc, int userId) {
        System.out.print("삭제할 리뷰 ID >> ");
        String input = sc.nextLine().trim();
        int reviewId;
        try {
            reviewId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("리뷰 ID는 숫자로 입력해야 합니다.");
            return;
        }

        String sql = "DELETE FROM reviews WHERE review_id = ? AND user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reviewId);
            ps.setInt(2, userId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[리뷰 삭제 완료]");
            } else {
                System.out.println("해당 리뷰가 없거나 본인 리뷰가 아닙니다.");
            }

        } catch (SQLException e) {
            System.out.println("[에러] 리뷰 삭제 중 오류");
            e.printStackTrace();
        }
    }
}
