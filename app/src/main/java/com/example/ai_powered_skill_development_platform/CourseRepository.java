package com.example.ai_powered_skill_development_platform;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CourseRepository {
    private static final String TAG = "CourseRepository";
    private static final String BASE_URL = "https://api.coursera.org/";
    private static final int CACHE_SIZE = 10 * 1024 * 1024; // 10 MB cache

    private CourseraApiService apiService;
    private GeminiRecommendationService recommendationService;
    private Context context;
    private boolean isNetworkAvailable = false;

    public CourseRepository(Context context) {
        this.context = context;
        this.isNetworkAvailable = isNetworkConnected(context);

        // Create logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Add cache for offline support
        Cache cache = new Cache(context.getCacheDir(), CACHE_SIZE);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .cache(cache);

        // Add offline interceptor if needed
        if (!isNetworkAvailable) {
            clientBuilder.addInterceptor(chain -> {
                // For offline mode, try to get cached responses
                return chain.proceed(chain.request().newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7) // 1 week
                        .build());
            });
        }

        OkHttpClient client = clientBuilder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(CourseraApiService.class);
        recommendationService = new GeminiRecommendationService(context);

        // Log network status for debugging
        Log.d(TAG, "Network connection available: " + isNetworkAvailable);
    }

    /**
     * Fetches courses for multiple subjects from Coursera API
     */
    public void getCoursesForMultipleSubjects(List<String> subjects, final CoursesCallback callback) {
        // If no network available, skip API calls and go straight to fallback
        if (!isNetworkAvailable) {
            Log.d(TAG, "No network connection available, using fallback courses");
            getFallbackCourses(callback);
            return;
        }

        // Fix: Filter out empty or null subjects
        List<String> validSubjects = new ArrayList<>();
        for (String subject : subjects) {
            if (subject != null && !subject.trim().isEmpty()) {
                validSubjects.add(subject);
            }
        }

        if (validSubjects.isEmpty()) {
            Log.d(TAG, "No valid subjects provided, using fallback courses");
            getFallbackCourses(callback);
            return;
        }

        // Convert subjects to Coursera domains with proper formatting
        String domains = CourseraDomainMapper.createDomainQueryString(validSubjects);

        // Fields to request from the API
        String fields = "name,slug,photoUrl,description,domainTypes,language,partnerLogo";
        String includes = "partners";
        int limit = 30; // Increased to get more options for filtering

        // Use a subject-derived query if possible, otherwise use a generic term
        String query = generateQueryFromSubjects(validSubjects);

        Log.d(TAG, "API Request - domains: " + domains + ", query: " + query);

        apiService.getCoursesByDomain(fields, includes, query, limit, domains)
                .enqueue(new Callback<CourseraCourseResponse>() {
                    @Override
                    public void onResponse(Call<CourseraCourseResponse> call, Response<CourseraCourseResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getElements() != null) {
                            // Filter for English courses only
                            List<CourseraCourse> englishCourses = filterEnglishCourses(response.body().getElements());

                            if (!englishCourses.isEmpty()) {
                                List<Course> courses = convertToCourses(englishCourses);
                                callback.onCoursesLoaded(courses);
                            } else {
                                Log.e(TAG, "No English courses found");
                                tryAlternativeRequest(callback, validSubjects);
                            }
                        } else {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading error body", e);
                            }

                            Log.e(TAG, "API error: " + response.code() + " - " + errorBody);
                            tryAlternativeRequest(callback, validSubjects);
                        }
                    }

                    @Override
                    public void onFailure(Call<CourseraCourseResponse> call, Throwable t) {
                        Log.e(TAG, "API call failed", t);
                        tryAlternativeRequest(callback, validSubjects);
                    }
                });
    }

    /**
     * Filters courses to return only English language courses
     */
    private List<CourseraCourse> filterEnglishCourses(List<CourseraCourse> courses) {
        List<CourseraCourse> englishCourses = new ArrayList<>();
        for (CourseraCourse course : courses) {
            if (course.getLanguage() == null ||
                    course.getLanguage().equalsIgnoreCase("en") ||
                    course.getLanguage().toLowerCase().startsWith("en-")) {
                englishCourses.add(course);
            }
        }
        return englishCourses;
    }

    /**
     * Generates a search query from subject list
     */
    private String generateQueryFromSubjects(List<String> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return "programming";
        }

        // Use the first subject as query basis, stripped of hyphens
        String baseQuery = subjects.get(0).replace("-", " ");

        // Check for specific keywords and enhance the query
        if (baseQuery.contains("machine learning") || baseQuery.contains("artificial intelligence")) {
            return "machine learning programming";
        } else if (baseQuery.contains("web")) {
            return "web development";
        } else if (baseQuery.contains("data")) {
            return "data science";
        } else if (baseQuery.contains("mobile")) {
            return "mobile development";
        } else if (baseQuery.contains("security") || baseQuery.contains("cyber")) {
            return "cybersecurity";
        }

        return baseQuery;
    }

    /**
     * Tries an alternative API request approach when the primary one fails
     */
    private void tryAlternativeRequest(final CoursesCallback callback, List<String> subjects) {
        // If no network available, skip API calls and go straight to fallback
        if (!isNetworkAvailable) {
            Log.d(TAG, "No network connection available for alternative request, using fallback courses");
            getFallbackCourses(callback);
            return;
        }

        // For alternative request, use a query based on user interests from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String fieldOfStudy = prefs.getString("fieldOfStudy", "");
        String careerGoal = prefs.getString("careerGoal", "");

        // Prioritize specific user interests over generic terms
        String query = !fieldOfStudy.isEmpty() ? fieldOfStudy :
                !careerGoal.isEmpty() ? careerGoal :
                        !subjects.isEmpty() ? subjects.get(0).replace("-", " ") : "programming";

        String fields = "name,slug,photoUrl,description,domainTypes,language,partnerLogo";
        String includes = "partners";
        int limit = 20;

        Log.d(TAG, "Trying alternative API request with user interest query: " + query);

        apiService.getCourses(fields, includes, query, limit).enqueue(new Callback<CourseraCourseResponse>() {
            @Override
            public void onResponse(Call<CourseraCourseResponse> call, Response<CourseraCourseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getElements() != null) {
                    // Filter for English courses only
                    List<CourseraCourse> englishCourses = filterEnglishCourses(response.body().getElements());

                    if (!englishCourses.isEmpty()) {
                        List<Course> courses = convertToCourses(englishCourses);
                        callback.onCoursesLoaded(courses);
                    } else {
                        Log.e(TAG, "No English courses found in alternative request");
                        getFallbackCourses(callback);
                    }
                } else {
                    Log.e(TAG, "Alternative API request failed");
                    getFallbackCourses(callback);
                }
            }

            @Override
            public void onFailure(Call<CourseraCourseResponse> call, Throwable t) {
                Log.e(TAG, "Alternative API call failed", t);
                getFallbackCourses(callback);
            }
        });
    }

    /**
     * Provides fallback courses when API calls fail
     */
    public void getFallbackCourses(final CoursesCallback callback) {
        List<Course> fallbackCourses = new ArrayList<>();

        // Get user interests for more personalized fallbacks
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String fieldOfStudy = prefs.getString("fieldOfStudy", "").toLowerCase();
        String careerGoal = prefs.getString("careerGoal", "").toLowerCase();

        // Base fallback courses
        List<Course> baseCourses = new ArrayList<>();

        baseCourses.add(new Course(
                "Introduction to Computer Science",
                "Learn the fundamentals of computer science and programming using Python by Harvard University",
                "https://d3njjcbhbojbot.cloudfront.net/api/utilities/v1/imageproxy/https://coursera-course-photos.s3.amazonaws.com/08/33f720502a11e59e72391aa537f5c9/pythonlearn_thumbnail_1x1.png",
                0,
                "https://www.coursera.org/learn/cs50"
        ));

        baseCourses.add(new Course(
                "Machine Learning Fundamentals",
                "Understanding the basics of ML algorithms and applications with practical exercises by Stanford",
                "https://d3njjcbhbojbot.cloudfront.net/api/utilities/v1/imageproxy/https://coursera-course-photos.s3.amazonaws.com/9d/6628a0263311e5996559538f179297/machine-learning-icon-2.png",
                0,
                "https://www.coursera.org/learn/machine-learning"
        ));

        baseCourses.add(new Course(
                "Web Development Bootcamp",
                "Full-stack web development with HTML, CSS, JavaScript, React, Node.js and MongoDB",
                "https://d3njjcbhbojbot.cloudfront.net/api/utilities/v1/imageproxy/https://coursera-course-photos.s3.amazonaws.com/83/e258e0532611e5837d7108edd7672c/front-end-icon.png",
                0,
                "https://www.coursera.org/learn/web-development"
        ));

        baseCourses.add(new Course(
                "Android App Development Specialization",
                "Build your own Android apps with Java and Kotlin. Create real-world projects and deploy to Google Play",
                "android.resource://" + context.getPackageName() + "/" + R.drawable.ic_placeholder,
                0,
                "https://www.coursera.org/specializations/android-app-development"
        ));

        baseCourses.add(new Course(
                "Data Science and Analytics",
                "Master data analysis techniques using Python, R, and visualization tools to extract insights",
                "android.resource://" + context.getPackageName() + "/" + R.drawable.ic_placeholder,
                0,
                "https://www.coursera.org/specializations/data-science"
        ));

        // Add courses based on user interests
        for (Course course : baseCourses) {
            // Always include at least 3 courses
            if (fallbackCourses.size() < 3 ||
                    course.getTitle().toLowerCase().contains(fieldOfStudy) ||
                    course.getTitle().toLowerCase().contains(careerGoal) ||
                    course.getDescription().toLowerCase().contains(fieldOfStudy) ||
                    course.getDescription().toLowerCase().contains(careerGoal)) {
                fallbackCourses.add(course);
            }
        }

        // Ensure we have at least 3 courses
        if (fallbackCourses.size() < 3) {
            for (Course course : baseCourses) {
                if (!fallbackCourses.contains(course)) {
                    fallbackCourses.add(course);
                }
                if (fallbackCourses.size() >= 5) {
                    break;
                }
            }
        }

        // Return the fallback courses
        callback.onCoursesLoaded(fallbackCourses);
    }

    /**
     * Converts Coursera API response courses to app's Course model
     */
    private List<Course> convertToCourses(List<CourseraCourse> courseraCourses) {
        List<Course> courses = new ArrayList<>();

        for (CourseraCourse courseraCourse : courseraCourses) {
            try {
                // Get partner name if available
                String partnerInfo = "";
                if (courseraCourse.getPartners() != null && !courseraCourse.getPartners().isEmpty()) {
                    partnerInfo = " by " + courseraCourse.getPartners().get(0).getName();
                }

                // Create description with partner info
                String description = courseraCourse.getDescription();
                if (description == null || description.isEmpty()) {
                    description = "A course on " + courseraCourse.getName() + partnerInfo;
                } else if (!partnerInfo.isEmpty()) {
                    description += "\n" + partnerInfo;
                }

                // Add domain info to description if available
                if (courseraCourse.getDomainTypes() != null && !courseraCourse.getDomainTypes().isEmpty()) {
                    StringBuilder domainInfo = new StringBuilder("\nTopics: ");
                    boolean first = true;
                    for (CourseraCourse.DomainType domain : courseraCourse.getDomainTypes()) {
                        if (!first) {
                            domainInfo.append(", ");
                        }
                        domainInfo.append(domain.toString()); // Using domain name instead of toString()
                        first = false;
                    }
                    description += domainInfo.toString();
                }

                // Create course URL
                String courseUrl = "https://www.coursera.org/learn/" + courseraCourse.getSlug();

                Course course = new Course(
                        courseraCourse.getName(),
                        description,
                        courseraCourse.getPhotoUrl(),
                        0,  // No progress for new courses
                        courseUrl
                );
                courses.add(course);
            } catch (Exception e) {
                Log.e(TAG, "Error converting course: " + e.getMessage());
                // Continue with next course
            }
        }

        return courses;
    }

    /**
     * Interface for course loading callbacks
     */
    public interface CoursesCallback {
        void onCoursesLoaded(List<Course> courses);

        // Default implementation for onError to make it optional
        default void onError(String errorMessage) {
            Log.e(TAG, errorMessage);
        }
    }

    /**
     * Gets courses recommended by Gemini based on user profile
     */
    public void getGeminiRecommendedCourses(final CoursesCallback callback) {
        // First check if there's network connectivity for Gemini API
        if (!isNetworkConnected(context)) {
            this.isNetworkAvailable = false;
            Log.d(TAG, "No network for Gemini recommendations, using fallback courses");
            getFallbackCourses(callback);
            return;
        }

        // Refresh network availability status
        this.isNetworkAvailable = true;

        // Get recommendations based on user profile data
        CompletableFuture<List<String>> recommendationsFuture = recommendationService.getRecommendedSubjects();

        recommendationsFuture.thenAccept(recommendations -> {
            if (recommendations != null && !recommendations.isEmpty()) {
                // Use all recommendations for a better selection
                Log.d(TAG, "Gemini recommendations: " + String.join(", ", recommendations));
                getCoursesForMultipleSubjects(recommendations, callback);
            } else {
                Log.e(TAG, "No recommendations received from Gemini");
                getFallbackCourses(callback);
            }
        }).exceptionally(ex -> {
            Log.e(TAG, "Error getting recommendations from Gemini", ex);
            getFallbackCourses(callback);
            return null;
        });
    }

    /**
     * Checks if network is connected
     */
    private boolean isNetworkConnected(Context context) {
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
}