package com.example.ai_powered_skill_development_platform;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CourseRepository {
    private static final String TAG = "CourseRepository";
    private static final String BASE_URL = "https://api.edx.org/";
    // You'll need to get an actual API key from edX's developer portal
    private static final String API_KEY = "YOUR_EDX_API_KEY";

    private EdxApiService apiService;

    public CourseRepository() {
        // Create logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(EdxApiService.class);
    }

    public void getRecommendedCourses(String subjectInterest, final CoursesCallback callback) {
        String authHeader = "Bearer " + API_KEY;

        apiService.searchCoursesBySubject(authHeader, subjectInterest, 1, 10).enqueue(new Callback<EdxCourseResponse>() {
            @Override
            public void onResponse(Call<EdxCourseResponse> call, Response<EdxCourseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Course> courses = convertToCourses(response.body().getResults());
                    callback.onCoursesLoaded(courses);
                } else {
                    // More detailed error handling
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }

                    Log.e(TAG, "API error: " + response.code() + " - " + errorBody);
                    callback.onError("Failed to load courses: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<EdxCourseResponse> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // Fallback to get courses when API fails
    public void getFallbackCourses(final CoursesCallback callback) {
        List<Course> fallbackCourses = new ArrayList<>();

        // Add some fallback courses in case the API fails
        fallbackCourses.add(new Course(
                "Introduction to Computer Science",
                "Learn the fundamentals of computer science and programming",
                "https://example.com/cs101.jpg",
                0
        ));

        fallbackCourses.add(new Course(
                "Machine Learning Fundamentals",
                "Understanding the basics of ML algorithms and applications",
                "https://example.com/ml101.jpg",
                0
        ));

        fallbackCourses.add(new Course(
                "Web Development Bootcamp",
                "Full-stack web development with modern technologies",
                "https://example.com/web101.jpg",
                0
        ));

        // Return the fallback courses
        callback.onCoursesLoaded(fallbackCourses);
    }

    private List<Course> convertToCourses(List<EdxCourse> edxCourses) {
        List<Course> courses = new ArrayList<>();

        for (EdxCourse edxCourse : edxCourses) {
            Course course = new Course(
                    edxCourse.getTitle(),
                    edxCourse.getShortDescription(),
                    edxCourse.getImageUrl(),
                    0  // No progress for new courses
            );
            courses.add(course);
        }

        return courses;
    }

    public interface CoursesCallback {
        void onCoursesLoaded(List<Course> courses);
        void onError(String errorMessage);
    }
}