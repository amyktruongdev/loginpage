package application;

import java.sql.Timestamp;

public class Blog {
    public int blogid;
    public String username;
    public String subject;
    public String description;
    public String tags;
    public Timestamp postDate;

    // Default constructor
    public Blog() {}

    // Full constructor
    public Blog(int blogid, String username, String subject, String description, String tags, Timestamp postDate) {
        this.blogid = blogid;
        this.username = username;
        this.subject = subject;
        this.description = description;
        this.tags = tags;
        this.postDate = postDate;
    }

    public int getBlogid() {
        return blogid;
    }

    public String getUsername() {
        return username;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public String getTags() {
        return tags;
    }

    public Timestamp getPostDate() {
        return postDate;
    }

    public void setBlogid(int blogid) {
        this.blogid = blogid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setPostDate(Timestamp postDate) {
        this.postDate = postDate;
    }
}
