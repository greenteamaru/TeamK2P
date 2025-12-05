package teamk2p.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class EventDao {
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

    // 목록 화면에서 쓸 DTO
    public static class EventListItem {
        private int eventId;
        private String title;
        private String clubName;
        private String venueName;
        private Timestamp startTime;

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
    }

    // 향후 7일 내 이벤트 목록
    public List<EventListItem> listUpcomingEvents(Connection conn) throws SQLException {
        String sql =
            "SELECT e.event_id, e.title, c.name AS club_name, v.name AS venue_name, e.start_time " +
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
                list.add(item);
            }
            return list;
        }
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
}
