package com.example.ai_powered_skill_development_platform;

public class LearningPath {
    private String name;
    private int progress; // Progress in percentage (0-100)

    public LearningPath(String name, int progress) {
        this.name = name;
        this.progress = progress;
    }

    public String getName() {
        return name;
    }

    public int getProgress() {
        return progress;
    }
}
