package com.example.map;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RetrofitInterface {

    @POST("/login")
    Call<LoginResult> executeLogin(@Body HashMap<String, String> map);

    @POST("/signup")
    Call<Void> executeSignup(@Body HashMap<String, String> map);

    @POST("/forgot-password")
    Call<Void> forgotPassword(@Body HashMap<String, String> map);

    @POST("/verify-otp")
    Call<Void> verifyOtp(@Body HashMap<String, String> map);

    @POST("/reset-password")
    Call<Void> resetPassword(@Body HashMap<String, String> map);

    @POST("/send-user-data")
    Call<UserData> sendUserData(@Body HashMap<String, String> map);

    @POST("/set-password")
    Call<Void> setPassword(@Body HashMap<String, String> map);

    @GET("/get-user-data")
    Call<UserData> getUserByEmail(@Query("email") String email);

    @POST("/update-user-data")
    Call<Void> updateUserData(@Body HashMap<String, String> map);

    @POST("/update-user-data")
    Call<Void> updateUserData(@Body UserData userData);
}