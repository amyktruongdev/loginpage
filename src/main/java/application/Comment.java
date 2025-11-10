package application;

import java.sql.Timestamp;

public class Comment {
    public String username;
    public String sentiment;
    public String commentText;
    public Timestamp date;

    // Constructor
    public Comment(String username, String sentiment, String commentText, Timestamp date) {
        this.username = username;
        this.sentiment = sentiment;
        this.commentText = commentText;
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public String getSentiment() {
        return sentiment;
    }

    public String getCommentText() {
        return commentText;
    }

    public Timestamp getCommentDate() {
        return date;
    }

    // (Optional) Setters — only if you ever need to modify data later
    public void setUsername(String username) {
        this.username = username;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public void setCommentDate(Timestamp date) {
        this.date = date;
    }
}
