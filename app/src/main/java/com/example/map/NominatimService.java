package com.example.map;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NominatimService {
    @GET("search")
    Call<List<NominatimResult>> searchLocation(@Query("q") String query, @Query("format") String format);

    @GET("search")
    Call<List<NominatimResult>> autocompleteLocation(@Query("q") String query, @Query("format") String format, @Query("addressdetails") String addressDetails);
}


