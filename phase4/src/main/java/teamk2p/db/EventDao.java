package teamk2p.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class EventDao {

    // ==========================
    // 1. 내 이벤트 목록용 DTO
    // ==========================
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

    // 내 이벤트 목록 조회
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

    // ==========================
    // 2. 이벤트 신청
    // ==========================
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

        // 정원 초과
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
            throw e; // 다른 에러는 위로 던짐
        }
    }

    // ==========================
    // 3. 이벤트 상세 보기용 DTO + 조회
    // ==========================
    public static class EventDetail {
        private int eventId;
        private String title;
        private String description;
        private String clubName;
        private String venueName;
        private java.sql.Timestamp startTime;
        private java.sql.Timestamp endTime;
        private int capacity;
        private int currentRegistrations;
        private BigDecimal fee;
        private String status;

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

        public java.sql.Timestamp getStartTime() { return startTime; }
        public void setStartTime(java.sql.Timestamp startTime) { this.startTime = startTime; }

        public java.sql.Timestamp getEndTime() { return endTime; }
        public void setEndTime(java.sql.Timestamp endTime) { this.endTime = endTime; }

        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }

        public int getCurrentRegistrations() { return currentRegistrations; }
        public void setCurrentRegistrations(int currentRegistrations) { this.currentRegistrations = currentRegistrations; }

        public BigDecimal getFee() { return fee; }
        public void setFee(BigDecimal fee) { this.fee = fee; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public EventDetail getEventDetail(Connection conn, int eventId) throws SQLException {
        String sql =
            "SELECT e.event_id, e.title, e.summary as description, e.start_time, e.end_time, " +
            "       e.capacity, e.fee, e.status, " +
            "       c.name AS club_name, v.name AS venue_name, " +
            "       (SELECT COUNT(*) FROM registrations r " +
            "         WHERE r.event_id = e.event_id " +
            "           AND r.status IN ('PENDING','CONFIRMED')) AS current_registrations " +
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
                    return d;
                } else {
                    return null;
                }
            }
        }
    }

    // ==========================
    // 4. 목록 화면용 DTO
    // ==========================
    public static class EventListItem {
        private int eventId;
        private String title;
        private String clubName;
        private String venueName;
        private Timestamp startTime;
        
        private int capacity;              // 정원
        private int currentRegistrations;  // 현재 신청 인원

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

 // 향후 7일 내 이벤트 목록
    public List<EventListItem> listUpcomingEvents(Connection conn) throws SQLException {
        String sql =
            "SELECT e.event_id, e.title, c.name AS club_name, v.name AS venue_name, " +
            "       e.start_time, e.capacity, " +
            "       (SELECT COUNT(*) " +
            "          FROM registrations r " +
            "         WHERE r.event_id = e.event_id " +
            "           AND r.status IN ('PENDING','CONFIRMED')) AS current_registrations " +
            "FROM events e, clubs c, venues v " +
            "WHERE e.club_id = c.club_id " +
            "  AND e.venue_id = v.venue_id " +
            "  AND e.start_time BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + 7 " +
            "ORDER BY e.start_time ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<EventListItem> list = new ArrayList<>();
            while (rs.next()) {
                EventListItem item = new EventListItem();
                item.setEventId(rs.getInt("event_id"));
                item.setTitle(rs.getString("title"));
                item.setClubName(rs.getString("club_name"));
                item.setVenueName(rs.getString("venue_name"));
                item.setStartTime(rs.getTimestamp("start_time"));
                item.setCapacity(rs.getInt("capacity"));                       // 🔽 추가
                item.setCurrentRegistrations(rs.getInt("current_registrations"));
                list.add(item);
            }
            return list;
        }
    }

    // 전체 이벤트 목록 (날짜 제한 없음)
    public List<EventListItem> listAllEvents(Connection conn) throws SQLException {
        String sql =
            "SELECT e.event_id, e.title, c.name AS club_name, v.name AS venue_name, " +
            "       e.start_time, e.capacity, " +
            "       (SELECT COUNT(*) " +
            "          FROM registrations r " +
            "         WHERE r.event_id = e.event_id " +
            "           AND r.status IN ('PENDING','CONFIRMED')) AS current_registrations " +
            "FROM events e, clubs c, venues v " +
            "WHERE e.club_id = c.club_id " +
            "  AND e.venue_id = v.venue_id " +
            "ORDER BY e.start_time DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<EventListItem> list = new ArrayList<>();
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
            return list;
        }
    }

    // 검색 + 7일 / 전체 토글
    public List<EventListItem> searchEvents(Connection conn, String keyword, boolean showAll)
            throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT e.event_id, e.title, ");
        sql.append("       c.name AS club_name, ");
        sql.append("       v.name AS venue_name, ");
        sql.append("       e.start_time ");
        sql.append("FROM events e ");
        sql.append("JOIN clubs c ON e.club_id = c.club_id ");
        sql.append("JOIN venues v ON e.venue_id = v.venue_id ");

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        // 기간 조건
        if (!showAll) {
            conditions.add("e.start_time BETWEEN TRUNC(SYSDATE) AND TRUNC(SYSDATE) + 7");
        }

        // 검색어 조건 (제목, 클럽명, 장소명)
        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.toLowerCase() + "%";
            conditions.add("(" +
                    "LOWER(e.title) LIKE ? " +
                    "OR LOWER(c.name) LIKE ? " +
                    "OR LOWER(v.name) LIKE ?" +
                    ")");
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" AND ", conditions));
        }

        sql.append(" ORDER BY e.start_time ");
        sql.append(showAll ? "DESC" : "ASC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Object p : params) {
                ps.setObject(idx++, p);
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<EventListItem> list = new ArrayList<>();
                while (rs.next()) {
                    EventListItem item = new EventListItem();
                    item.setEventId(rs.getInt("event_id"));
                    item.setTitle(rs.getString("title"));
                    item.setClubName(rs.getString("club_name"));
                    item.setVenueName(rs.getString("venue_name"));
                    item.setStartTime(rs.getTimestamp("start_time"));
                    list.add(item);
                }
                return list;
            }
        }
    }

    // ==========================
    // 5. 이벤트 신청 취소
    // ==========================
    /**
     * 이벤트 신청 취소
     *  - 현재 로그인한 userId + eventId 기준
     *  - 상태가 PENDING / CONFIRMED / WAITLISTED 인 것만 CANCELLED 로 변경
     *  - 결과코드: "CANCELLED", "NO_ACTIVE"
     */
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
                // 이미 취소되었거나 없는 경우
                return "NO_ACTIVE";
            }
            return "CANCELLED";
        }
    }
}
