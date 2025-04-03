package com.example.ai_powered_skill_development_platform;

import com.google.gson.annotations.SerializedName;

public class EdxCourse {
    @SerializedName("title")
    private String title;

    @SerializedName("short_description")
    private String shortDescription;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("marketing_url")
    private String marketingUrl;

    @SerializedName("pacing_type")
    private String pacingType;

    @SerializedName("start")
    private String startDate;

    @SerializedName("end")
    private String endDate;

    // Getters
    public String getTitle() {
        return title;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getMarketingUrl() {
        return marketingUrl;
    }

    public String getPacingType() {
        return pacingType;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }
}