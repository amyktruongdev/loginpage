package application;

import java.sql.*; 
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;

public class DatabaseConnection {

    private static final String URL =
        "jdbc:mysql://project440db.ch4680m2i6x4.us-east-2.rds.amazonaws.com:3306/440projectdb?useSSL=true&serverTimezone=UTC";
    private static final String USER = "admin";
    private static final String PASSWORD = "Project440!";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found", e);
            }
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static final Argon2 ARGON2 = Argon2Factory.create(Argon2Types.ARGON2id);
    private static final int ITER = 3;             // time cost
    private static final int MEM_KB = 15 * 1024;   // memory cost (15 MiB)
    private static final int PAR = 1;              // parallelism

    private static String hashPassword(String raw) {
        char[] pw = raw.toCharArray();
        try { return ARGON2.hash(ITER, MEM_KB, PAR, pw); }
        finally { ARGON2.wipeArray(pw); }
    }

    private static boolean verifyPassword(String raw, String phc) {
        char[] pw = raw.toCharArray();
        try { return ARGON2.verify(phc, pw); }
        finally { ARGON2.wipeArray(pw); }
    }

    public static String signup(String username, String password, String confirm,
                                String firstName, String lastName, String email, String phone) {

        if (username == null || username.isBlank()
                || password == null || password.length() < 8
                || confirm == null || !confirm.equals(password)
                || firstName == null || firstName.isBlank()
                || lastName == null || lastName.isBlank()
                || email == null || email.isBlank()
                || phone == null || phone.isBlank()) {
            return "Fill all fields (password ≥ 8) and ensure passwords match.";
        }

        String phc = hashPassword(password);

        final String sql = "INSERT INTO user(username, password, firstName, lastName, email, phone) "
                         + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, phc);
            ps.setString(3, firstName);
            ps.setString(4, lastName);
            ps.setString(5, email);
            ps.setString(6, phone);
            ps.executeUpdate();
            return "User registered successfully!";
        } catch (SQLIntegrityConstraintViolationException dup) {
            String m = dup.getMessage() == null ? "" : dup.getMessage().toLowerCase();
            if (m.contains("username")) return "Username already exists!";
            if (m.contains("email"))    return "Email already exists!";
            if (m.contains("phone"))    return "Phone already exists!";
            return "Duplicate value.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Database error.";
        }
    }

    public static boolean login(String username, String password) {
        if (username == null || username.isBlank() || password == null) return false;

        final String sql = "SELECT password FROM user WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String storedPhc = rs.getString(1);
                return verifyPassword(password, storedPhc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
