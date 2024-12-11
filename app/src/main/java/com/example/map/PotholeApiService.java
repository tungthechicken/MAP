package com.example.map;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PotholeApiService {
    // Khai báo phương thức trong PotholeApiService Interface

    @GET("/pothole")
    Call<List<Pothole>> getPotholes();

    @POST("/pothole")
    Call<Pothole> createPothole(@Body Pothole pothole);


    @POST("/pothole")
    Call<Void> savePothole(@Body Pothole pothole);

    @GET("/pothole/location")
    Call<List<Pothole>> getPotholeByLocation(@Query("latitude") double latitude, @Query("longitude") double longitude);

    @DELETE("/pothole/location")
    Call<Void> deletePothole(
            @Query("username") String username,
            @Query("latitude") String latitude,
            @Query("longitude") String longitude
    );
    @GET("/pothole/user")
    Call<List<Pothole>> getPotholeByUsername(@Query("username") String username);

}

