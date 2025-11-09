package application;
import java.sql.*;

public class AuthService {
    
    public AuthService() throws SQLException {
    }
    
public boolean login(String username, String enteredPassword) throws SQLException {
    try (var conn = DatabaseConnection.getConnection();
         var ps = conn.prepareStatement("SELECT password FROM user WHERE username=?")) {
        ps.setString(1, username);
        try (var rs = ps.executeQuery()) {
            if (!rs.next()) return false;
            String stored = rs.getString(1);

            if (stored != null && !stored.startsWith("$argon2")) {
                boolean ok = stored.equals(enteredPassword);
                if (ok) {
                    // 🔁 upgrade in place to Argon2
                    String newHash = Passwords.hash(enteredPassword);
                    try (var up = conn.prepareStatement("UPDATE user SET password=? WHERE username=?")) {
                        up.setString(1, newHash);
                        up.setString(2, username);
                        up.executeUpdate();
                    }
                }
                return ok;
            }

            return Passwords.verify(enteredPassword, stored);
        }
    }
}

public boolean register(User user) throws SQLException {
    try (var conn = DatabaseConnection.getConnection()) {
        String sql = "INSERT INTO user (username, password, firstName, lastName, email, phone) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        String phc = Passwords.hash(user.getPassword());

        try (var ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, phc);    
            ps.setString(3, user.getFirstName());
            ps.setString(4, user.getLastName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPhone());
            return ps.executeUpdate() > 0;
        }
    }
}



    private boolean checkDuplicate(Connection conn, String field, String value) throws SQLException {
        String sql = "SELECT 1 FROM user WHERE " + field + " = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}