package teamk2p.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    // 세션에 넣어서 쓸 DTO
    public static class LoginUser {
        private int userId;
        private String email;
        private String name;
        private boolean admin;   // 관리자 여부 추가

        public LoginUser(int userId, String email, String name, boolean admin) {
            this.userId = userId;
            this.email = email;
            this.name = name;
            this.admin = admin;
        }

        public int getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public boolean isAdmin() { return admin; }
    }

    // 이메일 + 비밀번호로 로그인 시도
    public LoginUser login(Connection conn, String email, String password) throws SQLException {
        String sql =
                "SELECT user_id, email, name, is_admin " +
                "FROM users " +
                "WHERE email = ? " +
                "  AND password = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String isAdminCol = rs.getString("is_admin"); // 'Y'/'N'
                    boolean isAdmin = "Y".equalsIgnoreCase(isAdminCol);

                    return new LoginUser(
                            rs.getInt("user_id"),
                            rs.getString("email"),
                            rs.getString("name"),
                            isAdmin
                    );
                }
            }
        }
        return null; // 로그인 실패
    }

    // 프로필용 DTO
    public static class UserProfile {
        private int userId;
        private String email;
        private String name;
        private String nickname;
        private String nationality;
        private String language;
        private java.sql.Date createdAt;

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getNationality() {
            return nationality;
        }

        public void setNationality(String nationality) {
            this.nationality = nationality;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public java.sql.Date getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(java.sql.Date createdAt) {
            this.createdAt = createdAt;
        }
    }

    public UserProfile getProfile(Connection conn, int userId) throws SQLException {
        String sql =
            "SELECT user_id, email, name, nickname, nationality, language, created_at " +
            "FROM users WHERE user_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserProfile p = new UserProfile();
                    p.setUserId(rs.getInt("user_id"));
                    p.setEmail(rs.getString("email"));
                    p.setName(rs.getString("name"));
                    p.setNickname(rs.getString("nickname"));
                    p.setNationality(rs.getString("nationality"));
                    p.setLanguage(rs.getString("language"));
                    p.setCreatedAt(rs.getDate("created_at"));
                    return p;
                }
            }
        }
        return null;
    }

    public void updateProfile(Connection conn, UserProfile p) throws SQLException {
        String sql =
            "UPDATE users " +
            "SET name = ?, nickname = ?, nationality = ?, language = ? " +
            "WHERE user_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getNickname());
            ps.setString(3, p.getNationality());
            ps.setString(4, p.getLanguage());
            ps.setInt(5, p.getUserId());

            ps.executeUpdate();
        }
    }
    
 // 관리자용 간단 회원 DTO
    public static class AdminUserItem {
        private int userId;
        private String name;
        private String email;
        private boolean admin;   // is_admin = 'Y'이면 true

        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public boolean isAdmin() { return admin; }
        public void setAdmin(boolean admin) { this.admin = admin; }
    }

    // Q7-ADMIN_ALL_USERS: 전체 회원 목록
    public java.util.List<AdminUserItem> listAllUsers(java.sql.Connection conn) throws java.sql.SQLException {
        String sql =
            "SELECT user_id, name, email, is_admin " +
            "FROM users " +
            "ORDER BY user_id";

        java.util.List<AdminUserItem> list = new java.util.ArrayList<>();

        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AdminUserItem item = new AdminUserItem();
                item.setUserId(rs.getInt("user_id"));
                item.setName(rs.getString("name"));
                item.setEmail(rs.getString("email"));
                String isAdmin = rs.getString("is_admin");
                item.setAdmin("Y".equalsIgnoreCase(isAdmin));
                list.add(item);
            }
        }
        return list;
    }

    // Q8-ADMIN_FIND_BY_EMAIL: 이메일 키워드 검색
    public java.util.List<AdminUserItem> searchUsersByEmail(java.sql.Connection conn, String emailKeyword)
            throws java.sql.SQLException {

        String sql =
            "SELECT user_id, name, email, is_admin " +
            "FROM users " +
            "WHERE email LIKE ? " +
            "ORDER BY user_id";

        java.util.List<AdminUserItem> list = new java.util.ArrayList<>();

        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + emailKeyword + "%");

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminUserItem item = new AdminUserItem();
                    item.setUserId(rs.getInt("user_id"));
                    item.setName(rs.getString("name"));
                    item.setEmail(rs.getString("email"));
                    String isAdmin = rs.getString("is_admin");
                    item.setAdmin("Y".equalsIgnoreCase(isAdmin));
                    list.add(item);
                }
            }
        }
        return list;
    }

}
