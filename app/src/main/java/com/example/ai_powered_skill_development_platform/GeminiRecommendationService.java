package com.example.ai_powered_skill_development_platform;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.TextPart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class GeminiRecommendationService {
    private static final String TAG = "GeminiRecommendation";
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY; // Moved to BuildConfig for security

    private final GenerativeModel model;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Context context;

    private View loadingIndicator;
    private CourseAdapter courseAdapter;
    private CourseRepository courseRepository;

    public GeminiRecommendationService(Context context) {
        this.context = context;
        model = new GenerativeModel("gemini-1.5-flash", API_KEY);
    }

    /**
     * Retrieves subject recommendations based on user profile data stored in SharedPreferences
     */
    public CompletableFuture<List<String>> getRecommendedSubjects() {
        // Extract user interests from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String educationLevel = prefs.getString("educationLevel", "");
        String furtherEducation = prefs.getString("furtherEducation", "");
        String fieldOfStudy = prefs.getString("fieldOfStudy", "");
        String careerGoal = prefs.getString("careerGoal", "");
        String workType = prefs.getString("workType", "");
        String skillsToMaster = prefs.getString("skillsToMaster", "");

        // Filter out empty strings
        List<String> userInterests = new ArrayList<>();
        if (!fieldOfStudy.isEmpty()) userInterests.add(fieldOfStudy);
        if (!careerGoal.isEmpty()) userInterests.add(careerGoal);
        if (!skillsToMaster.isEmpty()) userInterests.add(skillsToMaster);
        if (!workType.isEmpty()) userInterests.add(workType);
        if (!educationLevel.isEmpty()) userInterests.add(educationLevel);
        if (!furtherEducation.isEmpty()) userInterests.add(furtherEducation);

        List<String> previousCourses = getPreviousCourses();

        return getRecommendedSubjects(userInterests, previousCourses);
    }

    /**
     * Retrieves previously taken courses from local storage
     */
    private List<String> getPreviousCourses() {
        // This would be implemented to retrieve courses from your database or preferences
        // For now returning an empty list as a placeholder
        return new ArrayList<>();
    }

    /**
     * Gets subject recommendations based on explicit user interests and previous courses
     */
    public CompletableFuture<List<String>> getRecommendedSubjects(List<String> userInterests, List<String> previousCourses) {
        CompletableFuture<List<String>> resultFuture = new CompletableFuture<>();

        // Check network availability before making API call
        if (!isNetworkAvailable(context)) {  // Fixed: passing context parameter
            Log.d(TAG, "No network connection available, using default subjects");
            resultFuture.complete(getDefaultSubjects());
            return resultFuture;
        }

        try {
            String promptText = buildRecommendationPrompt(userInterests, previousCourses);
            Content content = new Content.Builder()
                    .addText(promptText)
                    .build();

            // Execute API call on background thread
            executor.execute(() -> {
                try {
                    // Using Kotlin's continuation to bridge with Java
                    model.generateContent(new Content[]{content}, new Continuation<GenerateContentResponse>() {
                        @Override
                        public CoroutineContext getContext() {
                            return EmptyCoroutineContext.INSTANCE;
                        }

                        @Override
                        public void resumeWith(Object result) {
                            try {
                                if (result instanceof kotlin.Result.Failure) {
                                    Throwable throwable = ((kotlin.Result.Failure) result).exception;
                                    Log.e(TAG, "Error in Gemini API call", throwable);
                                    resultFuture.complete(getDefaultSubjects());
                                } else {
                                    GenerateContentResponse response = (GenerateContentResponse) result;
                                    List<String> recommendations = extractRecommendations(response);
                                    resultFuture.complete(recommendations);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing Gemini response", e);
                                resultFuture.complete(getDefaultSubjects());
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error calling Gemini API", e);
                    resultFuture.complete(getDefaultSubjects());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error setting up Gemini request", e);
            resultFuture.complete(getDefaultSubjects());
        }

        return resultFuture;
    }

    /**
     * Extracts subject recommendations from Gemini API response
     */
    private List<String> extractRecommendations(GenerateContentResponse response) {
        if (response == null || response.getCandidates().isEmpty()) {
            Log.e(TAG, "Invalid or empty response from Gemini API");
            return getDefaultSubjects();
        }

        try {
            if (response.getCandidates().get(0).getContent().getParts().get(0) instanceof TextPart) {
                String text = ((TextPart) response.getCandidates().get(0).getContent().getParts().get(0)).getText();
                if (text != null && !text.isEmpty()) {
                    return parseRecommendations(text);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Gemini response", e);
        }

        return getDefaultSubjects();
    }

    /**
     * Builds a prompt for the Gemini API to generate subject recommendations
     */
    private String buildRecommendationPrompt(List<String> userInterests, List<String> previousCourses) {
        // If no interests provided, use a generic approach
        if (userInterests.isEmpty()) {
            return "Recommend 3 popular educational subjects in the technology field. " +
                    "Provide exactly 3 subject areas from: computer-science, data-science, artificial-intelligence, " +
                    "web-development, mobile-development, cybersecurity, cloud-computing, blockchain, " +
                    "machine-learning, data-visualization, project-management, digital-marketing, " +
                    "user-experience-design, business-analytics, entrepreneurship. " +
                    "Return ONLY the response in a comma-separated format (e.g., 'machine-learning, web-development, data-visualization') without any additional text.";
        }

        return "Based on the following specific user information, recommend 3 educational subjects: " +
                "\nUser interests: " + String.join(", ", userInterests) +
                "\nPreviously taken courses: " + (previousCourses.isEmpty() ? "None" : String.join(", ", previousCourses)) +
                "\nProvide exactly 3 subject areas for educational course recommendations that STRICTLY match the user's interests. " +
                "Choose from subjects like: computer-science, data-science, artificial-intelligence, " +
                "web-development, mobile-development, cybersecurity, cloud-computing, blockchain, " +
                "machine-learning, data-visualization, project-management, digital-marketing, " +
                "user-experience-design, business-analytics, entrepreneurship." +
                "\nAnalyze the user's interests carefully and select only topics that directly align with them. " +
                "\nProvide ONLY the response in a comma-separated format (e.g., 'machine-learning, web-development, data-visualization') without any additional text.";
    }

    /**
     * Parses the comma-separated recommendations text into a list of subjects
     */
    private List<String> parseRecommendations(String recommendationText) {
        List<String> parsedSubjects = new ArrayList<>();

        // Handle potential formatting issues in the response
        String cleanText = recommendationText.trim()
                .replaceAll("\n", "")
                .replaceAll("\\s+", " ");

        for (String subject : cleanText.split(",")) {
            String trimmedSubject = subject.trim();
            if (!trimmedSubject.isEmpty()) {
                parsedSubjects.add(trimmedSubject);
            }
        }

        // Ensure we have at most 3 recommendations
        return parsedSubjects.size() > 3 ? parsedSubjects.subList(0, 3) : parsedSubjects;
    }

    /**
     * Returns default subjects when API calls fail
     */
    private List<String> getDefaultSubjects() {
        // Get user preferences to personalize even the defaults
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String fieldOfStudy = prefs.getString("fieldOfStudy", "").toLowerCase();

        // Return different defaults based on user interests
        if (fieldOfStudy.contains("web") || fieldOfStudy.contains("front")) {
            return Arrays.asList("web-development", "user-experience-design", "digital-marketing");
        } else if (fieldOfStudy.contains("data") || fieldOfStudy.contains("analytics")) {
            return Arrays.asList("data-science", "data-visualization", "business-analytics");
        } else if (fieldOfStudy.contains("business") || fieldOfStudy.contains("management")) {
            return Arrays.asList("project-management", "entrepreneurship", "business-analytics");
        }

        // If we can't determine interests, rotate between a few different sets
        long dayOfYear = System.currentTimeMillis() / (24 * 60 * 60 * 1000);
        int setIndex = (int)(dayOfYear % 3);

        switch(setIndex) {
            case 0:
                return Arrays.asList("computer-science", "data-science", "artificial-intelligence");
            case 1:
                return Arrays.asList("web-development", "mobile-development", "cybersecurity");
            default:
                return Arrays.asList("machine-learning", "cloud-computing", "blockchain");
        }
    }

    /**
     * Checks if network is available for API calls
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
    }

    // Added non-static version of isNetworkAvailable for instance method usage
    private boolean isNetworkAvailable() {
        return isNetworkAvailable(context);
    }

    // These methods belong in an Activity or Fragment, not in this service
    // Moving them to a proper CourseActivity class would be better
    public void setReferences(View loadingIndicator, CourseAdapter courseAdapter,
                              CourseRepository courseRepository) {
        this.loadingIndicator = loadingIndicator;
        this.courseAdapter = courseAdapter;
        this.courseRepository = courseRepository;
    }

    public void loadInitialCourses() {
        if (loadingIndicator == null || courseAdapter == null || courseRepository == null) {
            Log.e(TAG, "References not set. Call setReferences() before loading courses");
            return;
        }

        // Show loading indicator
        loadingIndicator.setVisibility(View.VISIBLE);

        // Try to get recommendations first
        courseRepository.getGeminiRecommendedCourses(new CourseRepository.CoursesCallback() {
            @Override
            public void onCoursesLoaded(List<Course> courses) {
                // Hide loading indicator
                loadingIndicator.setVisibility(View.GONE);

                // Update UI with courses - using notifyDataSetChanged instead of setCourses
                updateCourseAdapter(courses);

                // Save these courses for offline use
                saveCourseDataForOffline(courses);
            }

            @Override
            public void onError(String errorMessage) {
                // Try loading fallback or cached courses
                loadOfflineCourses();
            }
        });
    }

    private void loadOfflineCourses() {
        // Load previously saved courses from SharedPreferences or Room database
        List<Course> savedCourses = getSavedCourses();
        if (savedCourses != null && !savedCourses.isEmpty()) {
            updateCourseAdapter(savedCourses);
        } else {
            // If no saved courses, get fallback
            courseRepository.getFallbackCourses(courses -> {
                updateCourseAdapter(courses);
            });
        }

        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the course adapter with new courses since CourseAdapter doesn't have a setCourses method
     */
    private void updateCourseAdapter(@NonNull List<Course> courses) {
        if (courseAdapter != null) {
            // Use a public method to update the courses instead of directly accessing the private field
            courseAdapter.updateCourses(courses);
            // If updateCourses already calls notifyDataSetChanged() internally, you don't need the line below
            courseAdapter.notifyDataSetChanged();
        }
    }

    // Placeholder method - implement according to your app's storage mechanism
    private void saveCourseDataForOffline(List<Course> courses) {
        // Implementation for saving courses for offline use
        Log.d(TAG, "Saving " + courses.size() + " courses for offline use");
    }

    // Placeholder method - implement according to your app's storage mechanism
    private List<Course> getSavedCourses() {
        // Implementation for retrieving saved courses
        Log.d(TAG, "Retrieving saved courses for offline use");
        return new ArrayList<>(); // Replace with actual implementation
    }
}