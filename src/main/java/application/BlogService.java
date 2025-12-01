package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlogService {

    // --------------------------------------------------------------------
    // BASIC BLOG OPERATIONS
    // --------------------------------------------------------------------

    // Check if user can post (limit 2 blogs per day)
    public boolean canUserPostBlog(String username) throws SQLException {
        String sql = "SELECT COUNT(*) AS blog_count " +
                     "FROM blogs " +
                     "WHERE username = ? AND DATE(post_date) = CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("blog_count");
                System.out.println("DEBUG: User " + username + " can post? Count: " + count);
                return count < 2;
            }
        }
        return false;
    }

    // Create a new blog (with tags) and return its ID
    public int createBlog(String username, String subject, String description, String tags)
            throws SQLException {

        // Check limit first
        if (!canUserPostBlog(username)) {
            throw new SQLException("You have reached the maximum limit of 2 blogs per day.");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Insert blog
                String blogSql = "INSERT INTO blogs (username, subject, description) VALUES (?, ?, ?)";
                int blogId;

                try (PreparedStatement blogStmt =
                             conn.prepareStatement(blogSql, Statement.RETURN_GENERATED_KEYS)) {
                    blogStmt.setString(1, username);
                    blogStmt.setString(2, subject);
                    blogStmt.setString(3, description);
                    blogStmt.executeUpdate();

                    try (ResultSet generatedKeys = blogStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            blogId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Creating blog failed, no ID obtained.");
                        }
                    }
                }

                // Insert tags
                if (tags != null && !tags.trim().isEmpty()) {
                    String tagSql = "INSERT INTO blog_tags (blogid, tag) VALUES (?, ?)";
                    try (PreparedStatement tagStmt = conn.prepareStatement(tagSql)) {
                        String[] tagArray = tags.split(",");
                        for (String tag : tagArray) {
                            String cleanTag = tag.trim().toLowerCase();
                            if (!cleanTag.isEmpty()) {
                                tagStmt.setInt(1, blogId);
                                tagStmt.setString(2, cleanTag);
                                tagStmt.addBatch();
                            }
                        }
                        tagStmt.executeBatch();
                    }
                }

                conn.commit();
                System.out.println("DEBUG: Blog created successfully with ID: " + blogId);
                return blogId;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // Get a single blog (with tags) by ID – used in the comment screen
    public Blog getBlogById(int blogId) throws SQLException {
        String sql = """
                SELECT b.username, b.subject, b.description,
                       GROUP_CONCAT(t.tag SEPARATOR ', ') AS tags,
                       b.post_date
                FROM blogs b
                LEFT JOIN blog_tags t ON b.blogid = t.blogid
                WHERE b.blogid = ?
                GROUP BY b.blogid, b.username, b.subject, b.description, b.post_date
                """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, blogId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Blog b = new Blog();
                b.blogid = blogId;
                b.username = rs.getString("username");
                b.subject = rs.getString("subject");
                b.description = rs.getString("description");
                b.tags = rs.getString("tags");
                b.postDate = rs.getTimestamp("post_date");
                return b;
            }
        }
        return null;
    }

    // Today’s blog count for user (for the 2-per-day label)
    public int getTodayBlogCount(String username) throws SQLException {
        String sql = "SELECT COUNT(*) AS blog_count " +
                     "FROM blogs " +
                     "WHERE username = ? AND DATE(post_date) = CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            int count = rs.next() ? rs.getInt("blog_count") : 0;

            System.out.println("DEBUG: User " + username + " has " + count + " blogs today");
            return count;
        }
    }

    // --------------------------------------------------------------------
    // ADVANCED QUERIES (items 1–7)
    // --------------------------------------------------------------------

    // 1) Users who posted blogs with Tag X and Tag Y on the same day
    public List<String> getUsersWithTwoTagsSameDay(String tagX, String tagY) throws SQLException {
        List<String> users = new ArrayList<>();

        String sql = """
            SELECT DISTINCT b1.username
            FROM blogs b1
            JOIN blog_tags t1 ON b1.blogid = t1.blogid
            JOIN blogs b2 ON b1.username = b2.username
            JOIN blog_tags t2 ON b2.blogid = t2.blogid
            WHERE DATE(b1.post_date) = DATE(b2.post_date)
              AND t1.tag LIKE ?
              AND t2.tag LIKE ?
              AND b1.blogid <> b2.blogid
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + tagX + "%");
            stmt.setString(2, "%" + tagY + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(rs.getString("username"));
                }
            }
        }

        return users;
    }

    // 2) Top bloggers on a given date (most blogs that day)
    public List<String> getTopBloggersOnDate(String dateStr) throws SQLException {
        List<String> topUsers = new ArrayList<>();

        String sql = """
            SELECT username
            FROM blogs
            WHERE DATE(post_date) = ?
            GROUP BY username
            HAVING COUNT(*) = (
                SELECT MAX(blog_count)
                FROM (
                    SELECT COUNT(*) AS blog_count
                    FROM blogs
                    WHERE DATE(post_date) = ?
                    GROUP BY username
                ) AS subquery
            )
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dateStr);
            stmt.setString(2, dateStr);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    topUsers.add(rs.getString("username"));
                }
            }
        }

        return topUsers;
    }

    // 3) Users who are followed by BOTH X and Y
    //    assumes table: follows(follower, followee)
    public List<String> getUsersFollowedByBoth(String userX, String userY) throws SQLException {
        List<String> result = new ArrayList<>();

        String sql = """
            SELECT DISTINCT f1.followee AS username
            FROM follows f1
            JOIN follows f2
              ON f1.followee = f2.followee
            WHERE f1.follower = ?
              AND f2.follower = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userX);
            stmt.setString(2, userY);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("username"));
                }
            }
        }

        return result;
    }

    // 4) Users who never posted a blog
    //    assumes table: user(username, ...)
    public List<String> getUsersWhoNeverPostedBlog() throws SQLException {
        List<String> result = new ArrayList<>();

        String sql = """
            SELECT u.username
            FROM user u
            LEFT JOIN blogs b
              ON u.username = b.username
            WHERE b.blogid IS NULL
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.add(rs.getString("username"));
            }
        }

        return result;
    }

    // 5) Blogs of user X where ALL comments are positive (and at least 1 comment)
    public List<String> getBlogsAllPositiveCommentsForUser(String username) throws SQLException {
        List<String> result = new ArrayList<>();

        String sql = """
            SELECT b.blogid, b.subject
            FROM blogs b
            JOIN comments c ON b.blogid = c.blogid
            WHERE b.username = ?
            GROUP BY b.blogid, b.subject
            HAVING COUNT(*) > 0
               AND SUM(CASE WHEN c.sentiment = 'negative' THEN 1 ELSE 0 END) = 0
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int blogId = rs.getInt("blogid");
                    String subject = rs.getString("subject");
                    result.add("Blog " + blogId + " - " + subject);
                }
            }
        }

        return result;
    }

    // 6) Users who posted SOME comments and every comment is negative
    public List<String> getUsersWithOnlyNegativeComments() throws SQLException {
        List<String> result = new ArrayList<>();

        String sql = """
            SELECT c.username
            FROM comments c
            GROUP BY c.username
            HAVING COUNT(*) > 0
               AND SUM(CASE WHEN c.sentiment <> 'negative' THEN 1 ELSE 0 END) = 0
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.add(rs.getString("username"));
            }
        }

        return result;
    }

    // 7) Users such that all blogs they posted never received any negative comments
    //    (at least one blog; blogs with only positive or no comments are OK)
    public List<String> getUsersWhoseBlogsNeverGotNegative() throws SQLException {
        List<String> result = new ArrayList<>();

        String sql = """
            SELECT b.username
            FROM blogs b
            LEFT JOIN comments c ON b.blogid = c.blogid
            GROUP BY b.username
            HAVING COUNT(*) > 0
               AND SUM(CASE WHEN c.sentiment = 'negative' THEN 1 ELSE 0 END) = 0
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.add(rs.getString("username"));
            }
        }

        return result;
    }
}
