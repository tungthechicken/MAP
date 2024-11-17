package com.example.map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenRouteServiceAPI {
    @GET("v2/directions/driving-car")
    Call<RouteResponse> getRoute(
            @Query("api_key") String apiKey,
            @Query("start") String start,
            @Query("end") String end
    );
}




