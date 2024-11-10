package com.example.map;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface PotholeApiService {
    // Khai báo phương thức trong PotholeApiService interface

    @GET("/pothole")
    Call<List<Pothole>> getPotholes();

    @POST("/pothole")
    Call<Pothole> createPothole(@Body Pothole pothole);

    @PUT("/pothole/{id}")
    Call<Pothole> updatePothole(@Path("id") String id, @Body Pothole pothole);

    @DELETE("/pothole/{id}")
    Call<Void> deletePothole(@Path("id") String id);

    @POST("/pothole")
    Call<Void> savePothole(@Body Pothole pothole);
}

