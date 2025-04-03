package com.example.ai_powered_skill_development_platform;

import android.util.Log;

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
    private static final String API_KEY = "AIzaSyCcZsSeou_55-zjLp4iq3QtIcwvabtEcPE"; // Replace with actual API key

    private final GenerativeModel model;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public GeminiRecommendationService() {
        model = new GenerativeModel("gemini-1.5-flash", API_KEY);
    }

    public CompletableFuture<List<String>> getRecommendedSubjects(List<String> userInterests, List<String> previousCourses) {
        CompletableFuture<List<String>> resultFuture = new CompletableFuture<>();

        try {
            String promptText = buildRecommendationPrompt(userInterests, previousCourses);
            Content content = new Content.Builder()
                    .addText(promptText)
                    .build();

            // Create a CompletableFuture that will be completed when the API call completes
            executor.execute(() -> {
                try {
                    // Use a custom continuation to bridge between Kotlin and Java
                    // Wrap the content object in an array as required by the method signature
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

    private List<String> extractRecommendations(GenerateContentResponse response) {
        if (response != null && !response.getCandidates().isEmpty()) {
            String text = "";

            // Extract text from the response
            if (response.getCandidates().get(0).getContent().getParts().get(0) instanceof TextPart) {
                text = ((TextPart) response.getCandidates().get(0).getContent().getParts().get(0)).getText();
            }

            if (!text.isEmpty()) {
                return parseRecommendations(text);
            }
        }
        return getDefaultSubjects();
    }

    private String buildRecommendationPrompt(List<String> userInterests, List<String> previousCourses) {
        return "Based on the following user information, recommend 3 specific educational subjects: " +
                "\nUser interests: " + String.join(", ", userInterests) +
                "\nPreviously taken courses: " + String.join(", ", previousCourses) +
                "\nProvide exactly 3 subject areas in a comma-separated format (e.g., 'machine-learning, web-development, data-visualization').";
    }

    private List<String> parseRecommendations(String recommendationText) {
        List<String> parsedSubjects = new ArrayList<>();
        for (String subject : recommendationText.trim().split(",")) {
            parsedSubjects.add(subject.trim());
        }
        return parsedSubjects.size() > 3 ? parsedSubjects.subList(0, 3) : parsedSubjects;
    }

    private List<String> getDefaultSubjects() {
        return Arrays.asList("computer-science", "data-science", "artificial-intelligence");
    }
}