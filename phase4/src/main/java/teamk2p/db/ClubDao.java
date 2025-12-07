package teamk2p.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClubDao {

    // =============================
    // 1. 내 클럽 목록용 DTO
    // =============================
    public static class MyClubItem {
        private int clubId;
        private String name;
        private String clubType;
        private String role;
        private String status;
        private String description;
        private String categoryName;
        private int memberCount;

        public int getClubId() { return clubId; }
        public void setClubId(int clubId) { this.clubId = clubId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getClubType() { return clubType; }
        public void setClubType(String clubType) { this.clubType = clubType; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public int getMemberCount() { return memberCount; }
        public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
    }

    // =============================
    // 2. 클럽 상세 화면용 DTO
    // =============================
    public static class ClubDetail {
        private int clubId;
        private String name;
        private String clubType;
        private String categoryName;
        private String status;
        private String description;
        private Timestamp createdAt;
        private int activeMembers;   // 현재 ACTIVE 멤버 수
        private int totalMembers;    // 누적 가입자 수 (정원이 아님)

        // 현재 로그인한 사용자의 membership 정보
        private String myStatus;     // ACTIVE / LEFT / BANNED / null(가입 이력 없음)
        private String myRole;       // LEADER / OFFICER / MEMBER 등
        private Timestamp myJoinedAt;
        private Timestamp myLeftAt;

        public int getClubId() { return clubId; }
        public void setClubId(int clubId) { this.clubId = clubId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getClubType() { return clubType; }
        public void setClubType(String clubType) { this.clubType = clubType; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

        public int getActiveMembers() { return activeMembers; }
        public void setActiveMembers(int activeMembers) { this.activeMembers = activeMembers; }

        public int getTotalMembers() { return totalMembers; }
        public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }

        public String getMyStatus() { return myStatus; }
        public void setMyStatus(String myStatus) { this.myStatus = myStatus; }

        public String getMyRole() { return myRole; }
        public void setMyRole(String myRole) { this.myRole = myRole; }

        public Timestamp getMyJoinedAt() { return myJoinedAt; }
        public void setMyJoinedAt(Timestamp myJoinedAt) { this.myJoinedAt = myJoinedAt; }

        public Timestamp getMyLeftAt() { return myLeftAt; }
        public void setMyLeftAt(Timestamp myLeftAt) { this.myLeftAt = myLeftAt; }
    }

    // =============================
    // 3. 전체 클럽 목록용 DTO
    // =============================
    public static class ClubSummary {
        private int clubId;
        private String name;
        private String clubType;
        private String categoryName;
        private String status;
        private String description;
        // 현재 ACTIVE 멤버 수
        private int memberCount;
        // 지금까지 누적 가입자 수 (정원이 아님)
        private int totalMembers;
        // 클럽 생성 시각
        private Timestamp createdAt;

        public int getClubId() { return clubId; }
        public void setClubId(int clubId) { this.clubId = clubId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getClubType() { return clubType; }
        public void setClubType(String clubType) { this.clubType = clubType; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public int getMemberCount() { return memberCount; }
        public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

        public int getTotalMembers() { return totalMembers; }
        public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }

        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    }

    // =============================
    // 4. 내 클럽 목록 조회
    // =============================
    public List<MyClubItem> listMyClubs(Connection conn, int userId) throws SQLException {
        String sql =
            "SELECT m.club_id, c.name, c.club_type, m.role, m.status " +
            "FROM memberships m " +
            "JOIN clubs c ON m.club_id = c.club_id " +
            "WHERE m.user_id = ? " +
            "ORDER BY c.name";

        List<MyClubItem> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MyClubItem item = new MyClubItem();
                    item.setClubId(rs.getInt("club_id"));
                    item.setName(rs.getString("name"));
                    item.setClubType(rs.getString("club_type"));
                    item.setRole(rs.getString("role"));
                    item.setStatus(rs.getString("status"));
                    list.add(item);
                }
            }
        }
        return list;
    }

    // =============================
    // 5. 전체 클럽 목록 (상태 무관, 단순 정보)
    // =============================
    public List<ClubSummary> listAllClubs(Connection conn) throws SQLException {
        String sql =
            "SELECT c.club_id, c.name, c.club_type, cat.name AS category_name, " +
            "       c.status, c.description, c.created_at " +
            "FROM clubs c " +
            "JOIN categories cat ON c.category_id = cat.category_id " +
            "ORDER BY c.name";

        List<ClubSummary> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ClubSummary c = new ClubSummary();
                c.setClubId(rs.getInt("club_id"));
                c.setName(rs.getString("name"));
                c.setClubType(rs.getString("club_type"));
                c.setCategoryName(rs.getString("category_name"));
                c.setStatus(rs.getString("status"));
                c.setDescription(rs.getString("description"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(c);
            }
        }
        return list;
    }

    // =============================
    // 6. ACTIVE 클럽 목록 + 멤버수/정렬
    // =============================
    public List<ClubSummary> listActiveClubs(Connection conn, String sort) throws SQLException {
        String orderBy;
        if ("members_desc".equalsIgnoreCase(sort)) {
            orderBy = "active_members DESC, c.name";
        } else if ("members_asc".equalsIgnoreCase(sort)) {
            orderBy = "active_members ASC, c.name";
        } else if ("latest".equalsIgnoreCase(sort)) {
            orderBy = "c.created_at DESC";
        } else {
            orderBy = "c.name";
        }

        String sql =
            "SELECT c.club_id, c.name, c.club_type, cat.name AS category_name, " +
            "       c.status, c.description, c.created_at, " +
            "       NVL(SUM(CASE WHEN m.status = 'ACTIVE' THEN 1 ELSE 0 END), 0) AS active_members, " +
            "       NVL(COUNT(m.membership_id), 0) AS total_members " +
            "FROM clubs c " +
            "JOIN categories cat ON c.category_id = cat.category_id " +
            "LEFT JOIN memberships m " +
            "       ON m.club_id = c.club_id " +
            "WHERE c.status = 'ACTIVE' " +
            "GROUP BY c.club_id, c.name, c.club_type, cat.name, " +
            "         c.status, c.description, c.created_at " +
            "ORDER BY " + orderBy;

        List<ClubSummary> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ClubSummary c = new ClubSummary();
                c.setClubId(rs.getInt("club_id"));
                c.setName(rs.getString("name"));
                c.setClubType(rs.getString("club_type"));
                c.setCategoryName(rs.getString("category_name"));
                c.setStatus(rs.getString("status"));
                c.setDescription(rs.getString("description"));
                c.setCreatedAt(rs.getTimestamp("created_at"));
                c.setMemberCount(rs.getInt("active_members"));
                c.setTotalMembers(rs.getInt("total_members"));
                list.add(c);
            }
        }
        return list;
    }

    // =============================
    // 7. 클럽 가입 로직
    // =============================
    public String joinClub(Connection conn, int userId, int clubId) throws SQLException {
        String checkSql =
            "SELECT status FROM memberships WHERE user_id = ? AND club_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, clubId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");

                    if ("ACTIVE".equalsIgnoreCase(status)) {
                        return "ALREADY";
                    } else {
                        String updSql =
                            "UPDATE memberships " +
                            "SET status = 'ACTIVE', joined_at = SYSDATE, left_at = NULL " +
                            "WHERE user_id = ? AND club_id = ?";

                        try (PreparedStatement ups = conn.prepareStatement(updSql)) {
                            ups.setInt(1, userId);
                            ups.setInt(2, clubId);
                            ups.executeUpdate();
                        }
                        return "REJOIN";
                    }
                }
            }
        }

        String insSql =
            "INSERT INTO memberships (user_id, club_id, role, status) " +
            "VALUES (?, ?, 'MEMBER', 'ACTIVE')";

        try (PreparedStatement ps = conn.prepareStatement(insSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, clubId);
            ps.executeUpdate();
        }

        return "JOINED";
    }

    // =============================
    // 8. 클럽 상세 조회
    // =============================
    public ClubDetail getClubDetail(Connection conn, int clubId, Integer userId) throws SQLException {

        String sql =
            "SELECT c.club_id, c.name, c.club_type, cat.name AS category_name, " +
            "       c.status, c.description, c.created_at, " +
            "       NVL(SUM(CASE WHEN m.status = 'ACTIVE' THEN 1 ELSE 0 END), 0) AS active_members, " +
            "       NVL(COUNT(m.membership_id), 0) AS total_members " +
            "FROM clubs c " +
            "JOIN categories cat ON c.category_id = cat.category_id " +
            "LEFT JOIN memberships m " +
            "       ON m.club_id = c.club_id " +
            "WHERE c.club_id = ? " +
            "GROUP BY c.club_id, c.name, c.club_type, cat.name, " +
            "         c.status, c.description, c.created_at";

        ClubDetail detail = null;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    detail = new ClubDetail();
                    detail.setClubId(rs.getInt("club_id"));
                    detail.setName(rs.getString("name"));
                    detail.setClubType(rs.getString("club_type"));
                    detail.setCategoryName(rs.getString("category_name"));
                    detail.setStatus(rs.getString("status"));
                    detail.setDescription(rs.getString("description"));
                    detail.setCreatedAt(rs.getTimestamp("created_at"));
                    detail.setActiveMembers(rs.getInt("active_members"));
                    detail.setTotalMembers(rs.getInt("total_members"));
                }
            }
        }

        if (detail == null) {
            return null;
        }

        if (userId != null) {
            String memSql =
                "SELECT status, role, joined_at, left_at " +
                "FROM memberships " +
                "WHERE user_id = ? AND club_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(memSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, clubId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        detail.setMyStatus(rs.getString("status"));
                        detail.setMyRole(rs.getString("role"));
                        detail.setMyJoinedAt(rs.getTimestamp("joined_at"));
                        detail.setMyLeftAt(rs.getTimestamp("left_at"));
                    }
                }
            }
        }

        return detail;
    }
}
