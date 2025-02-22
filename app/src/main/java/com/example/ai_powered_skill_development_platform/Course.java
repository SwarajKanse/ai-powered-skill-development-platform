package com.example.ai_powered_skill_development_platform;

public class Course {
    private String title;
    private String description;
    private int thumbnailResId; // For local drawable resources
    private String thumbnailUrl; // For online images
    private int progress;

    public Course(String title, String description, int thumbnailResId, int progress) {
        this.title = title;
        this.description = description;
        this.thumbnailResId = thumbnailResId;
        this.progress = progress;
        this.thumbnailUrl = null; // Default to null
    }

    public Course(String title, String description, String thumbnailUrl, int progress) {
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.progress = progress;
        this.thumbnailResId = 0; // Default to zero for online images
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getThumbnailResId() {
        return thumbnailResId;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isOnlineImage() {
        return thumbnailUrl != null && !thumbnailUrl.isEmpty();
    }
}
