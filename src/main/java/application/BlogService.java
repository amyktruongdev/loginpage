package application;

import java.sql.*;
import java.util.*;

public class BlogService {
	
    
    public boolean canUserPostBlog(String username) throws SQLException {
        String sql = "SELECT COUNT(*) as blog_count FROM blogs WHERE username = ? AND DATE(post_date) = CURDATE()";
        
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
    
    public int createBlog(String username, String subject, String description, String tags) throws SQLException {
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
                
                try (PreparedStatement blogStmt = conn.prepareStatement(blogSql, Statement.RETURN_GENERATED_KEYS)) {
                    blogStmt.setString(1, username);
                    blogStmt.setString(2, subject);
                    blogStmt.setString(3, description);
                    blogStmt.executeUpdate();
                    
                    // Get generated blog ID
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

    public int getTodayBlogCount(String username) throws SQLException {
        String sql = "SELECT COUNT(*) as blog_count FROM blogs WHERE username = ? AND DATE(post_date) = CURDATE()";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            ResultSet rs = stmt.executeQuery();
            int count = rs.next() ? rs.getInt("blog_count") : 0;
            
            System.out.println("DEBUG: User " + username + " has " + count + " blogs today");
            return count;
        }
    }
    
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
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(rs.getString("username"));
            }
        }
        return users;
    }

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
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                topUsers.add(rs.getString("username"));
            }
        }

        return topUsers;
    }
}