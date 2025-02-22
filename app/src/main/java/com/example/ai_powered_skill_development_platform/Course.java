package com.example.ai_powered_skill_development_platform;

public class Course {
    private String title;
    private String description;
    private int progress;
    private int thumbnailResId;   // For local drawable resources
    private String thumbnailUrl;  // For online images
    private boolean onlineImage;  // True if an online image is used
    private boolean undertaken;   // True if the user has undertaken this course
    private String websiteUrl;    // URL of the course website

    // Constructor for local image courses without website URL (if needed)
    public Course(String title, String description, int thumbnailResId, int progress) {
        this.title = title;
        this.description = description;
        this.thumbnailResId = thumbnailResId;
        this.progress = progress;
        this.thumbnailUrl = null;
        this.onlineImage = false;
        this.undertaken = false;
        this.websiteUrl = ""; // default empty
    }

    // Constructor for online image courses without website URL (if needed)
    public Course(String title, String description, String thumbnailUrl, int progress) {
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.progress = progress;
        this.thumbnailResId = 0;
        this.onlineImage = true;
        this.undertaken = false;
        this.websiteUrl = ""; // default empty
    }

    // Constructor with websiteUrl for local image courses
    public Course(String title, String description, int progress, int thumbnailResId, String websiteUrl, boolean onlineImage, boolean undertaken) {
        this.title = title;
        this.description = description;
        this.progress = progress;
        this.thumbnailResId = thumbnailResId;
        this.websiteUrl = websiteUrl;
        this.onlineImage = onlineImage;
        this.undertaken = undertaken;
        this.thumbnailUrl = null; // not used for local images
    }

    // Overloaded constructor with websiteUrl for local image courses (undertaken defaults to false)
    public Course(String title, String description, int progress, int thumbnailResId, String websiteUrl, boolean onlineImage) {
        this(title, description, progress, thumbnailResId, websiteUrl, onlineImage, false);
    }

    // Constructor with websiteUrl for online image courses
    public Course(String title, String description, String thumbnailUrl, int progress, String websiteUrl) {
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.progress = progress;
        this.websiteUrl = websiteUrl;
        this.thumbnailResId = 0;
        this.onlineImage = true;
        this.undertaken = false;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getProgress() {
        return progress;
    }

    public int getThumbnailResId() {
        return thumbnailResId;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public boolean isOnlineImage() {
        return onlineImage;
    }

    public boolean isUndertaken() {
        return undertaken;
    }

    public void setUndertaken(boolean undertaken) {
        this.undertaken = undertaken;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }
}
