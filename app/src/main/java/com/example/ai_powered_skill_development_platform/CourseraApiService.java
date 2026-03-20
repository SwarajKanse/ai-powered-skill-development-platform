package com.example.ai_powered_skill_development_platform;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CourseraApiService {
    @GET("api/courses.v1")
    Call<CourseraCourseResponse> getCourses(
            @Query("fields") String fields,
            @Query("includes") String includes,
            @Query("query") String query, // Changed from 'q' to 'query'
            @Query("limit") int limit
    );

    @GET("api/courses.v1")
    Call<CourseraCourseResponse> getCoursesByDomain(
            @Query("fields") String fields,
            @Query("includes") String includes,
            @Query("query") String query, // Changed from 'q' to 'query'
            @Query("limit") int limit,
            @Query("domains") String domains
    );
}