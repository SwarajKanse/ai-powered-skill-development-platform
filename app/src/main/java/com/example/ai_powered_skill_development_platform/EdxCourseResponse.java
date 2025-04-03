package com.example.ai_powered_skill_development_platform;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EdxCourseResponse {
    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<EdxCourse> results;

    public int getCount() {
        return count;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public List<EdxCourse> getResults() {
        return results;
    }
}