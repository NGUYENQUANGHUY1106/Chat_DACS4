package database;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
   
    private static final String URL =
        "jdbc:mysql://192.168.195.189:3306/chat_client?useSSL=false&serverTimezone=UTC";

    private static final String USER = "chatuser";
    private static final String PASSWORD = "1234";   

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            System.out.println("Lỗi kết nối MySQL: " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }
}
