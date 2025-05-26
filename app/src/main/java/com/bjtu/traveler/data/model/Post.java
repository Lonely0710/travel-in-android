package com.bjtu.traveler.data.model;

public class Post {
    private long id;
    private long userId;
    private String title;
    private String content;
    private long timestamp;
    private String imageUrl;

    public Post(long id, long userId, String title, String content, long timestamp, String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.imageUrl = imageUrl;
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    public String getImageUrl() { return imageUrl; }

    public void setId(long id) { this.id = id; }
    public void setUserId(long userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}

