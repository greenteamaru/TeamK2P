package teamk2p.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class EventDao {

    // =============================
    // 1. 내 이벤트 목록용 DTO
    // =============================
    public static class MyEventItem {
        private int eventId;
        private String title;
        private Timestamp startTime;
        private String venueName;
        private String status;    // registrations.status
        private String attended;  // 'Y' / 'N'

        public int getEventId() { return eventId; }
        public void setEventId(int eventId) { this.eventId = eventId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Timestamp getStartTime() { return startTime; }
        public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

        public String getVenueName() { return venueName; }
        public void setVenueName(String venueName) { this.venueName = venueName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getAttended() { return attended; }
        public void setAttended(String attended) { this.attended = attended; }
    }

    public List<MyEventItem> listMyEvents(Connection conn, int userId) throws SQLException {
        String sql =
            "SELECT r.event_id, e.title, e.start_time, v.name AS venue_name, " +
            "       r.status, r.attended " +
            "FROM registrations r " +
            "JOIN events e ON r.event_id = e.event_id " +
            "JOIN venues v ON e.venue_id = v.venue_id " +
            "WHERE r.user_id = ? " +
            "ORDER BY e.start_time DESC";

        List<MyEventItem> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MyEventItem item = new MyEventItem();
                    item.setEventId(rs.getInt("event_id"));
                    item.setTitle(rs.getString("title"));
                    item.setStartTime(rs.getTimestamp("start_time"));
                    item.setVenueName(rs.getString("venue_name"));
                    item.setStatus(rs.getString("status"));
                    item.setAttended(rs.getString("attended"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    // =============================
    // 2. 이벤트 신청 (정원 체크 + 동시성)
    // =============================
    public String registerForEvent(Connection conn, int userId, int eventId) throws SQLException {
        // 결과 코드: OK, FULL, DUP, NO_EVENT
        String checkSql =
            "SELECT e.capacity, " +
            "       (SELECT COUNT(*) " +
            "          FROM registrations r " +
            "         WHERE r.event_id = e.event_id " +
            "           AND r.status IN ('PENDING','CONFIRMED')) AS current_cnt " +
            "FROM events e " +
            "WHERE e.event_id = ? " +
            "FOR UPDATE";

        int capacity;
        int current;

        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return "NO_EVENT";
                }
                capacity = rs.getInt("capacity");
                current  = rs.getInt("current_cnt");
            }
        }

        if (current >= capacity) {
            return "FULL";
        }

        String insertSql =
            "INSERT INTO registrations (user_id, event_id, status) " +
            "VALUES (?, ?, 'CONFIRMED')";

        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            ps.executeUpdate();
            return "OK";
        } catch (SQLException e) {
            // ORA-00001: unique constraint (uq_registrations_user_event) violated
            if (e.getErrorCode() == 1) {
                return "DUP";
            }
            throw e;
        }
    }

    // =============================
    // 3. 이벤트 상세 DTO
    // =============================
    public static class EventDetail {
        private int eventId;
        private String title;
        private String description;
        private String clubName;
        private String venueName;
        private Timestamp startTime;
        private Timestamp endTime;
        private int capacity;
        private int currentRegistrations;
        private BigDecimal fee;
        private String status;
        
        // 리뷰,평점 정보
        private Double avgRating;   // null 이면 아직 리뷰 없음
        private int reviewCount;

        public int getEventId() { return eventId; }
        public void setEventId(int eventId) { this.eventId = eventId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getClubName() { return clubName; }
        public void setClubName(String clubName) { this.clubName = clubName; }

        public String getVenueName() { return venueName; }
        public void setVenueName(String venueName) { this.venueName = venueName; }

        public Timestamp getStartTime() { return startTime; }
        public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

        public Timestamp getEndTime() { return endTime; }
        public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }

        public int getCurrentRegistrations() { return currentRegistrations; }
        public void setCurrentRegistrations(int currentRegistrations) { this.currentRegistrations = currentRegistrations; }

        public BigDecimal getFee() { return fee; }
        public void setFee(BigDecimal fee) { this.fee = fee; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        // 평점, 리뷰
        public Double getAvgRating() { return avgRating; }
        public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }

        public int getReviewCount() { return reviewCount; }
        public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    }
    
    

    // =============================
    // 4. 이벤트 목록용 DTO (카드용)
    // =============================
    public static class EventListItem {
        private int eventId;
        private String title;
        private String clubName;
        private String venueName;
        private Timestamp startTime;
        private int capacity;
        private int currentRegistrations;

        public int getEventId() { return eventId; }
        public void setEventId(int eventId) { this.eventId = eventId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getClubName() { return clubName; }
        public void setClubName(String clubName) { this.clubName = clubName; }

        public String getVenueName() { return venueName; }
        public void setVenueName(String venueName) { this.venueName = venueName; }

        public Timestamp getStartTime() { return startTime; }
        public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }

        public int getCurrentRegistrations() { return currentRegistrations; }
        public void setCurrentRegistrations(int currentRegistrations) { this.currentRegistrations = currentRegistrations; }
    }

    // =============================
    // 5. 향후 7일 내 이벤트 목록
    // =============================
    public List<EventListItem> listUpcomingEvents(Connection conn) throws SQLException {
        String sql =
            "SELECT e.event_id, e.title, c.name AS club_name, v.name AS venue_name, " +
            "       e.start_time, e.capacity, " +
            "       (SELECT COUNT(*) FROM registrations r " +
            "         WHERE r.event_id = e.event_id " +
            "           AND r.status IN ('PENDING','CONFIRMED')) AS current_registrations " +
            "FROM events e " +
            "JOIN clubs c ON e.club_id = c.club_id " +
            "JOIN venues v ON e.venue_id = v.venue_id " +
            "WHERE e.start_time BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + 7 " +
            "ORDER BY e.start_time ASC";

        List<EventListItem> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                EventListItem item = new EventListItem();
                item.setEventId(rs.getInt("event_id"));
                item.setTitle(rs.getString("title"));
                item.setClubName(rs.getString("club_name"));
                item.setVenueName(rs.getString("venue_name"));
                item.setStartTime(rs.getTimestamp("start_time"));
                item.setCapacity(rs.getInt("capacity"));
                item.setCurrentRegistrations(rs.getInt("current_registrations"));
                list.add(item);
            }
        }
        return list;
    }

    // =============================
    // 6. 전체 이벤트 목록 (날짜 제한 없음)
    // =============================
    public List<EventListItem> listAllEvents(Connection conn) throws SQLException {
        String sql =
            "SELECT e.event_id, e.title, c.name AS club_name, v.name AS venue_name, " +
            "       e.start_time, e.capacity, " +
            "       (SELECT COUNT(*) FROM registrations r " +
            "         WHERE r.event_id = e.event_id " +
            "           AND r.status IN ('PENDING','CONFIRMED')) AS current_registrations " +
            "FROM events e " +
            "JOIN clubs c ON e.club_id = c.club_id " +
            "JOIN venues v ON e.venue_id = v.venue_id " +
            "ORDER BY e.start_time DESC";

        List<EventListItem> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                EventListItem item = new EventListItem();
                item.setEventId(rs.getInt("event_id"));
                item.setTitle(rs.getString("title"));
                item.setClubName(rs.getString("club_name"));
                item.setVenueName(rs.getString("venue_name"));
                item.setStartTime(rs.getTimestamp("start_time"));
                item.setCapacity(rs.getInt("capacity"));
                item.setCurrentRegistrations(rs.getInt("current_registrations"));
                list.add(item);
            }
        }
        return list;
    }

    // =============================
    // 7. 검색 + 7일 / 전체 통합 메서드
    //     EventListServlet에서 호출하는 메서드
    // =============================
    public List<EventListItem> searchEvents(Connection conn, String keyword, boolean showAll)
            throws SQLException {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT e.event_id, e.title, c.name AS club_name, v.name AS venue_name, ");
        sb.append("       e.start_time, e.capacity, ");
        sb.append("       (SELECT COUNT(*) FROM registrations r ");
        sb.append("         WHERE r.event_id = e.event_id ");
        sb.append("           AND r.status IN ('PENDING','CONFIRMED')) AS current_registrations ");
        sb.append("FROM events e ");
        sb.append("JOIN clubs c ON e.club_id = c.club_id ");
        sb.append("JOIN venues v ON e.venue_id = v.venue_id ");
        sb.append("WHERE 1 = 1 ");

        List<String> params = new ArrayList<>();

        // showAll이 false면 향후 7일만
        if (!showAll) {
            sb.append(" AND e.start_time BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + 7 ");
        }

        // 키워드 검색 (이벤트명 / 클럽명 / 장소명 / 캠퍼스명)
        if (keyword != null && !keyword.isEmpty()) {
            sb.append(" AND (");
            sb.append("      LOWER(e.title) LIKE ? ");
            sb.append("   OR LOWER(c.name) LIKE ? ");
            sb.append("   OR LOWER(v.name) LIKE ? ");
            // venues에 campus 컬럼 있으면 주석 해제 / 없으면 이 줄 지워도 됨
            sb.append("   OR LOWER(v.campus) LIKE ? ");
            sb.append(") ");

            String like = "%" + keyword.toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }

        // 정렬: 7일 모드면 오래된 순, 전체 모드는 최신순
        sb.append(" ORDER BY e.start_time ");
        if (showAll) {
            sb.append("DESC");
        } else {
            sb.append("ASC");
        }

        String sql = sb.toString();

        List<EventListItem> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EventListItem item = new EventListItem();
                    item.setEventId(rs.getInt("event_id"));
                    item.setTitle(rs.getString("title"));
                    item.setClubName(rs.getString("club_name"));
                    item.setVenueName(rs.getString("venue_name"));
                    item.setStartTime(rs.getTimestamp("start_time"));
                    item.setCapacity(rs.getInt("capacity"));
                    item.setCurrentRegistrations(rs.getInt("current_registrations"));
                    list.add(item);
                }
            }
        }

        return list;
    }

    // =============================
    // 8. 이벤트 상세
    // =============================
    public EventDetail getEventDetail(Connection conn, int eventId) throws SQLException {
        String sql =
            "SELECT e.event_id, e.title, e.summary AS description, e.start_time, e.end_time, " +
            "       e.capacity, e.fee, e.status, " +
            "       c.name AS club_name, v.name AS venue_name, " +
            "       (SELECT COUNT(*) " +
            "          FROM registrations r " +
            "         WHERE r.event_id = e.event_id " +
            "           AND r.status IN ('PENDING','CONFIRMED')) AS current_registrations, " +
            "       (SELECT AVG(rv.rating) " +
            "          FROM reviews rv " +
            "         WHERE rv.event_id = e.event_id " +
            "           AND rv.status = 'VISIBLE') AS avg_rating, " +
            "       (SELECT COUNT(*) " +
            "          FROM reviews rv2 " +
            "         WHERE rv2.event_id = e.event_id " +
            "           AND rv2.status = 'VISIBLE') AS review_count " +
            "FROM events e " +
            "JOIN clubs c ON e.club_id = c.club_id " +
            "JOIN venues v ON e.venue_id = v.venue_id " +
            "WHERE e.event_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    EventDetail d = new EventDetail();
                    d.setEventId(rs.getInt("event_id"));
                    d.setTitle(rs.getString("title"));
                    d.setDescription(rs.getString("description"));
                    d.setStartTime(rs.getTimestamp("start_time"));
                    d.setEndTime(rs.getTimestamp("end_time"));
                    d.setCapacity(rs.getInt("capacity"));
                    d.setFee(rs.getBigDecimal("fee"));
                    d.setStatus(rs.getString("status"));
                    d.setClubName(rs.getString("club_name"));
                    d.setVenueName(rs.getString("venue_name"));
                    d.setCurrentRegistrations(rs.getInt("current_registrations"));

                    // ✅ 리뷰 집계 매핑
                    int rc = rs.getInt("review_count");
                    d.setReviewCount(rc);
                    if (rc > 0) {
                        d.setAvgRating(rs.getDouble("avg_rating"));
                    } else {
                        d.setAvgRating(null);
                    }

                    return d;
                } else {
                    return null;
                }
            }
        }
    }

    // =============================
    // 9. 신청 취소
    // =============================
    public String cancelRegistration(Connection conn, int userId, int eventId) throws SQLException {
        String sql =
            "UPDATE registrations " +
            "SET status = 'CANCELLED' " +
            "WHERE user_id = ? " +
            "  AND event_id = ? " +
            "  AND status IN ('PENDING','CONFIRMED','WAITLISTED')";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                return "NO_ACTIVE";
            }
            return "CANCELLED";
        }
    }

    // =============================
    // 10. (선택) 캠퍼스별 7일 내 이벤트
    // =============================
    public List<EventListItem> listUpcomingEventsByCampus(Connection conn, String campus) throws SQLException {
        String sql =
            "SELECT e.event_id, e.title, c.name AS club_name, v.name AS venue_name, " +
            "       e.start_time, e.capacity, " +
            "       (SELECT COUNT(*) FROM registrations r " +
            "         WHERE r.event_id = e.event_id " +
            "           AND r.status IN ('PENDING','CONFIRMED')) AS current_registrations " +
            "FROM events e " +
            "JOIN clubs c ON e.club_id = c.club_id " +
            "JOIN venues v ON e.venue_id = v.venue_id " +
            "WHERE v.campus = ? " +
            "  AND e.start_time BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + 7 " +
            "ORDER BY e.start_time ASC";

        List<EventListItem> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, campus);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EventListItem item = new EventListItem();
                    item.setEventId(rs.getInt("event_id"));
                    item.setTitle(rs.getString("title"));
                    item.setClubName(rs.getString("club_name"));
                    item.setVenueName(rs.getString("venue_name"));
                    item.setStartTime(rs.getTimestamp("start_time"));
                    item.setCapacity(rs.getInt("capacity"));
                    item.setCurrentRegistrations(rs.getInt("current_registrations"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    // =============================
    // 11. 클럽 상세용 이벤트 목록 DTO
    // =============================
    public static class ClubEventItem {
        private int eventId;
        private String title;
        private Timestamp startTime;
        private String status;
        private Double avgRating;
        private int reviewCount;

        public int getEventId() { return eventId; }
        public void setEventId(int eventId) { this.eventId = eventId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Timestamp getStartTime() { return startTime; }
        public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Double getAvgRating() { return avgRating; }
        public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }

        public int getReviewCount() { return reviewCount; }
        public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    }

    // 클럽의 앞으로 열릴 이벤트
    public List<ClubEventItem> listClubUpcomingEvents(Connection conn, int clubId) throws SQLException {
        String sql =
            "SELECT e.event_id, e.title, e.start_time, e.status, " +
            "       AVG(rv.rating) AS avg_rating, " +
            "       COUNT(rv.review_id) AS review_count " +
            "FROM events e " +
            "LEFT JOIN reviews rv " +
            "       ON rv.event_id = e.event_id " +
            "      AND rv.status = 'VISIBLE' " +
            "WHERE e.club_id = ? " +
            "  AND e.start_time >= TRUNC(SYSDATE) " +
            "GROUP BY e.event_id, e.title, e.start_time, e.status " +
            "ORDER BY e.start_time ASC";

        List<ClubEventItem> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ClubEventItem item = new ClubEventItem();
                    item.setEventId(rs.getInt("event_id"));
                    item.setTitle(rs.getString("title"));
                    item.setStartTime(rs.getTimestamp("start_time"));
                    item.setStatus(rs.getString("status"));

                    int rc = rs.getInt("review_count");
                    item.setReviewCount(rc);
                    if (rc > 0) {
                        item.setAvgRating(rs.getDouble("avg_rating"));
                    } else {
                        item.setAvgRating(null);
                    }
                    list.add(item);
                }
            }
        }
        return list;
    }

    // 클럽의 과거 이벤트
    public List<ClubEventItem> listClubPastEvents(Connection conn, int clubId) throws SQLException {
        String sql =
            "SELECT e.event_id, e.title, e.start_time, e.status, " +
            "       AVG(rv.rating) AS avg_rating, " +
            "       COUNT(rv.review_id) AS review_count " +
            "FROM events e " +
            "LEFT JOIN reviews rv " +
            "       ON rv.event_id = e.event_id " +
            "      AND rv.status = 'VISIBLE' " +
            "WHERE e.club_id = ? " +
            "  AND e.start_time < TRUNC(SYSDATE) " +
            "GROUP BY e.event_id, e.title, e.start_Time, e.status " +
            "ORDER BY e.start_time DESC";

        List<ClubEventItem> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ClubEventItem item = new ClubEventItem();
                    item.setEventId(rs.getInt("event_id"));
                    item.setTitle(rs.getString("title"));
                    item.setStartTime(rs.getTimestamp("start_time"));
                    item.setStatus(rs.getString("status"));

                    int rc = rs.getInt("review_count");
                    item.setReviewCount(rc);
                    if (rc > 0) {
                        item.setAvgRating(rs.getDouble("avg_rating"));
                    } else {
                        item.setAvgRating(null);
                    }
                    list.add(item);
                }
            }
        }
        return list;
    }
}
