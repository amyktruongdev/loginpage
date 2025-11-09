package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SearchService {

    private static final String SQL =
        "SELECT b.blogid, b.username, b.subject, b.description, b.post_date, " +
        "       GROUP_CONCAT(bt.tag ORDER BY bt.tag SEPARATOR ', ') AS tags " +
        "FROM blogs b " +
        "JOIN blog_tags bt ON bt.blogid = b.blogid " +
        "WHERE LOWER(bt.tag) = LOWER(?) " +
        "GROUP BY b.blogid, b.username, b.subject, b.description, b.post_date " +
        "ORDER BY b.post_date DESC";

    public static class Row {
        public final int blogid;
        public final String username;
        public final String subject;
        public final String description;
        public final String tags;
        public final Timestamp postDate;

        public Row(int blogid, String username, String subject, String description, String tags, Timestamp postDate) {
            this.blogid = blogid;
            this.username = username;
            this.subject = subject;
            this.description = description;
            this.tags = tags;
            this.postDate = postDate;
        }
    }

    public List<Row> searchByTag(String tag) throws SQLException {
        List<Row> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SQL)) {
            ps.setString(1, tag.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Row(
                        rs.getInt("blogid"),
                        rs.getString("username"),
                        rs.getString("subject"),
                        rs.getString("description"),
                        rs.getString("tags"),
                        rs.getTimestamp("post_date")
                    ));
                }
            }
        }
        return out;
    }
}
