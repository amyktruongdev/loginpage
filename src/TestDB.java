import java.sql.*;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            System.out.println("Connected to DB successfully!");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
