package com.example.map;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitInterface {

    @POST("/login")
    Call<LoginResult> executeLogin(@Body HashMap<String, String> map);

    @POST("/signup")
    Call<Void> executeSignup(@Body HashMap<String, String> map);

    @POST("/forgot-password")
    Call<Void> forgotPassword(@Body HashMap<String, String> map);

    @POST("/reset-password/{token}")
    Call<Void> resetPassword(@Path("token") String token, @Body HashMap<String, String> map);
}
