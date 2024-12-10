package com.example.map;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface PotholeDataCallback {
    void onPotholesRetrieved(List<LatLng> potholeLocations);
    void onError(String errorMessage);
}
