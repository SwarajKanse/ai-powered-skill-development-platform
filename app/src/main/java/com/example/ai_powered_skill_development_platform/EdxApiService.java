package com.example.ai_powered_skill_development_platform;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface EdxApiService {
    @GET("course_discovery/v1/course_runs/")
    Call<EdxCourseResponse> getCourses(
            @Header("Authorization") String authToken,
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("course_discovery/v1/course_runs/")
    Call<EdxCourseResponse> searchCoursesBySubject(
            @Header("Authorization") String authToken,
            @Query("subject") String subject,
            @Query("page") int page,
            @Query("page_size") int pageSize
    );
}