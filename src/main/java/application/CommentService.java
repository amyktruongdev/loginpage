package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentService {
    private static final int MAX_COMMENTS_PER_DAY = 3;

    public boolean canUserComment(String username) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM comments WHERE username = ? AND DATE(comment_date) = CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                return count < MAX_COMMENTS_PER_DAY;
            }
        }
        return false;
    }
    public boolean isUserAuthorOfBlog(String username, int blogId) throws SQLException {
        String sql = "SELECT username FROM blogs WHERE blogid = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, blogId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return username.equals(rs.getString("username"));
            }
        }
        return false;
    }

    public boolean hasAlreadyCommented(String username, int blogId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comments WHERE username = ? AND blogid = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, blogId);
            ResultSet rs = stmt.executeQuery();

            rs.next();
            return rs.getInt(1) > 0;
        }
    }

    public boolean addComment(String username, int blogId, String sentiment, String text) throws SQLException {
        if (isUserAuthorOfBlog(username, blogId))
            throw new SQLException("You cannot comment on your own blog!");

        if (hasAlreadyCommented(username, blogId))
            throw new SQLException("You have already commented on this blog!");

        if (!canUserComment(username))
            throw new SQLException("You have reached your daily limit of 3 comments.");

        String sql = "INSERT INTO comments (blogid, username, sentiment, comment_text) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, blogId);
            stmt.setString(2, username);
            stmt.setString(3, sentiment);
            stmt.setString(4, text);
            stmt.executeUpdate();
            return true;
        }
    }

    public List<Comment> getCommentsByBlogId(int blogId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT username, sentiment, comment_text, comment_date FROM comments WHERE blogid = ? ORDER BY comment_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, blogId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Comment c = new Comment(
                    rs.getString("username"),
                    rs.getString("sentiment"),
                    rs.getString("comment_text"),
                    rs.getTimestamp("comment_date")
                );
                comments.add(c);
            }
        }
        return comments;
    }
}
