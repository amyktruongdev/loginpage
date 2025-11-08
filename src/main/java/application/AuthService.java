package application;
import java.sql.*;

public class AuthService {
    
    public AuthService() throws SQLException {
        // Don't store connection - create new one for each operation
    }
    
    public boolean login(String username, String password) throws SQLException {
        // Use try-with-resources to automatically close connection
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT username FROM user WHERE username = ? AND password = ?";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, username);
                statement.setString(2, password);
                ResultSet resultSet = statement.executeQuery();
                return resultSet.next();
            }
        }
    }
    
    public boolean register(User user) throws SQLException {
        // Use try-with-resources to automatically close connection
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check for duplicates
            if (checkDuplicate(conn, "username", user.getUsername())) {
                throw new SQLException("Username already exists");
            }
            if (checkDuplicate(conn, "email", user.getEmail())) {
                throw new SQLException("Email already registered");
            }
            if (checkDuplicate(conn, "phone", user.getPhone())) {
                throw new SQLException("Phone already registered");
            }

            // Insert new user
            String sql = "INSERT INTO user (username, password, firstName, lastName, email, phone) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, user.getUsername());
                statement.setString(2, user.getPassword());
                statement.setString(3, user.getFirstName());
                statement.setString(4, user.getLastName());
                statement.setString(5, user.getEmail());
                statement.setString(6, user.getPhone());
                return statement.executeUpdate() > 0;
            }
        }
    }

    private boolean checkDuplicate(Connection conn, String field, String value) throws SQLException {
        String sql = "SELECT " + field + " FROM user WHERE " + field + " = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, value);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }
}