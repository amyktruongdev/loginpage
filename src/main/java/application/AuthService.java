package application;
import java.sql.*;

public class AuthService {
    private Connection connection;

    public AuthService() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    public boolean login(String username, String password) throws SQLException {
        return authenticateUser(username, password);
    }

    public boolean register(User user) throws SQLException {
        return registerUser(user);
    }

    private boolean authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT username FROM user WHERE username = ? AND password = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    private boolean registerUser(User user) throws SQLException {
        if (checkDuplicateField("username", user.getUsername())) {
            throw new SQLException("Username already exists");
        }
        if (checkDuplicateField("email", user.getEmail())) {
            throw new SQLException("Email already registered");
        }
        if (checkDuplicateField("phone", user.getPhone())) {
            throw new SQLException("Phone number already registered");
        }

        String sql = "INSERT INTO user (username, password, firstName, lastName, email, phone) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getFirstName());
            statement.setString(4, user.getLastName());
            statement.setString(5, user.getEmail());
            statement.setString(6, user.getPhone());

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }
    }

    private boolean checkDuplicateField(String fieldName, String value) throws SQLException {
        String sql = "SELECT " + fieldName + " FROM user WHERE " + fieldName + " = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }
}
