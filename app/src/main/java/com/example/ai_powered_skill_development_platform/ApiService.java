package com.example.ai_powered_skill_development_platform;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("youtube/v3/search")
    Call<ApiResponse> getYoutubeCourses(
            @Query("part") String part,
            @Query("q") String query,
            @Query("key") String apiKey
    );

    @GET("some-coursera-endpoint")
    Call<ApiResponse> getCourseraCourses(@Query("param") String param);
}
