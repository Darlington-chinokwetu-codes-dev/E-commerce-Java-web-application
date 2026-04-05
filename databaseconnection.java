package database;
import java.sql.Connection;
import java.sql.*;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class DBConnection {
    private static final String url = "jdbc:mysql://localhost:3306/e_commerce";
    private static final String username = "root";
    private static final String password = "Dadzadee@06";
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Database connected successfully");
        } catch (SQLException e) {
            System.out.println("Database connection failed");
            e.printStackTrace();
        }
        return conn;
    }
}
