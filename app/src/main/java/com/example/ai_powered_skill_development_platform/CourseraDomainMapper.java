package com.example.ai_powered_skill_development_platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseraDomainMapper {
    private static final Map<String, String> subjectToDomainMap = new HashMap<>();

    static {
        // Computer Science Related - using API-safe format
        subjectToDomainMap.put("computer-science", "computer-science");
        subjectToDomainMap.put("data-science", "data-science");
        subjectToDomainMap.put("artificial-intelligence", "artificial-intelligence");
        subjectToDomainMap.put("machine-learning", "machine-learning");
        subjectToDomainMap.put("web-development", "web-development");
        subjectToDomainMap.put("mobile-development", "mobile-development");
        subjectToDomainMap.put("cybersecurity", "security");
        subjectToDomainMap.put("cloud-computing", "cloud-computing");
        subjectToDomainMap.put("blockchain", "blockchain");
        subjectToDomainMap.put("data-visualization", "data-analysis");
        subjectToDomainMap.put("java", "java-programming");
        subjectToDomainMap.put("kotlin", "android-development");
        // Business Related
        subjectToDomainMap.put("project-management", "project-management");
        subjectToDomainMap.put("digital-marketing", "marketing");
        subjectToDomainMap.put("user-experience-design", "design");
        subjectToDomainMap.put("business-analytics", "business-analytics");
        subjectToDomainMap.put("entrepreneurship", "entrepreneurship");
    }

    /**
     * Maps a Gemini recommendation subject to a Coursera domain
     * @param subject The subject from Gemini recommendations
     * @return The matching Coursera domain or a generic fallback if no mapping exists
     */
    public static String mapToDomain(String subject) {
        // Clean the subject to ensure safe format
        String cleanSubject = subject.trim().toLowerCase().replace(" ", "-");

        // Return the mapped domain or a generic "computer" if no mapping exists
        return subjectToDomainMap.getOrDefault(cleanSubject, "computer");
    }

    /**
     * Creates a comma-separated string of domains for the API
     * @param subjects List of subjects from Gemini
     * @return Comma-separated string of mapped domains
     */
    public static String createDomainQueryString(List<String> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return "computer,data-science"; // Default domains if none provided
        }

        StringBuilder domains = new StringBuilder();
        boolean first = true;

        for (String subject : subjects) {
            if (subject != null && !subject.trim().isEmpty()) {
                if (!first) domains.append(",");
                domains.append(mapToDomain(subject));
                first = false;
            }
        }

        // If no valid domains were added, return default domains
        if (domains.length() == 0) {
            return "computer,data-science";
        }

        return domains.toString();
    }
}