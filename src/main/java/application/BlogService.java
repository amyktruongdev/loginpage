package application;

import java.sql.*;
import java.time.LocalDate;

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
}