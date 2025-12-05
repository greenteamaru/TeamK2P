package teamk2p.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    private static final String URL  = "jdbc:oracle:thin:@//localhost:1521/ORCL";
    private static final String USER = "project";
    private static final String PASS = "project";

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Oracle JDBC Driver load failed", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASS);
        conn.setAutoCommit(false);
        return conn;
    }
}