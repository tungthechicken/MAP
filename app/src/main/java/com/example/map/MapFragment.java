package com.example.map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;


public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng selectedLocation;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorEventListener;
    private boolean isShaking = false;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private Sensor magnetometer;
    private Sensor gyroscope;

    private PotholeApiService apiService;
    private long lastShakeTime = 0; // Thời gian lắc cuối cùng
    private static final long SHAKE_INTERVAL = 1000; // Khoảng thời gian (1 giây) giữa các lần lắc

    private Circle userLocationCircle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://5538-113-185-79-135.ngrok-free.app") // Thay bằng URL của server bạn
                //.baseUrl("http://10.0.2.2:3000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(PotholeApiService.class);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.id_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ImageButton btnShowLocation = view.findViewById(R.id.btn_show_location);
        btnShowLocation.setOnClickListener(v -> getLocation());

        ImageButton btnAddMarker = view.findViewById(R.id.btn_add_marker);
        btnAddMarker.setOnClickListener(v -> markLocation());

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                float shakeThreshold = 18; // Ngưỡng lắc (bạn có thể điều chỉnh)

                long currentTime = System.currentTimeMillis(); // Lấy thời gian hiện tại

                if (Math.sqrt(x * x + y * y + z * z) > shakeThreshold) {
                    // Kiểm tra nếu lắc đủ mạnh và đã qua thời gian chờ giữa các lần lắc
                    if (!isShaking && (currentTime - lastShakeTime) > SHAKE_INTERVAL) {
                        isShaking = true;
                        lastShakeTime = currentTime; // Cập nhật thời gian lắc cuối cùng
                        getLocationForShake(); // Gọi hàm xử lý lắc
                    }
                } else {
                    isShaking = false;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // No need to handle this
            }
        };

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key));
        }

        SearchView searchView = view.findViewById(R.id.search_location);
        setupSearchView(searchView);

        return view;


    }

    private void setupSearchView(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(requireActivity());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void showLocationInfo(LatLng location) {
        requireActivity().runOnUiThread(() -> {
            String locationInfo = "Your location: Latitude: " + location.latitude +
                    ", Longitude: " + location.longitude;
            Toast.makeText(requireContext(), locationInfo, Toast.LENGTH_LONG).show();
        });
    }

    private void getLocationForShake() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        if (googleMap != null) {
                            googleMap.clear();
                            googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }

                        showLocationInfo(currentLocation);

                        // Hiển thị hộp thoại xác nhận
                        showConfirmationDialog(currentLocation);
                    } else {
                        Toast.makeText(requireContext(), "Unable to get location!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void showConfirmationDialog(LatLng location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirm Pothole");
        builder.setMessage("Is this a pothole?");

        builder.setPositiveButton("Pothole", (dialog, which) -> {
            // Khi người dùng chọn Pothole, lưu lại vị trí và thông tin
            savePothole(location);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Người dùng chọn Cancel, không làm gì cả
            dialog.dismiss();
        });

        builder.show();
    }

    private void savePothole(LatLng location) {
        // Tạo dữ liệu ổ gà
        Pothole pothole = new Pothole(
                "username", // điền tên người dùng ở đây
                1.0, // kích thước giả định
                0.5, // độ sâu giả định
                0.5, // đường kính giả định
                "Pothole", // nhãn ổ gà
                new Pothole.LocationData(
                        String.valueOf(location.longitude),
                        String.valueOf(location.latitude),
                        "address" // điền địa chỉ nếu có
                )
        );

        // Gọi API để lưu ổ gà
        apiService.savePothole(pothole).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Pothole saved on server!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to save pothole!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == requireActivity().RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    googleMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(requireContext(), "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorEventListener, gyroscope, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(selectedLocation).title("Selected Location"));
        });
    }

    private void startRadarEffect() {
        if (userLocationCircle != null) {
            final int maxRadius = 200;  // Bán kính tối đa của radar
            final int minRadius = 50;   // Bán kính tối thiểu của radar
            final int duration = 2000;  // Thời gian thay đổi bán kính trong ms

            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (userLocationCircle != null) {
                        double radius = userLocationCircle.getRadius();

                        if (radius >= maxRadius) {
                            userLocationCircle.setRadius(minRadius);  // Đặt lại bán kính nếu vượt quá tối đa
                        } else {
                            userLocationCircle.setRadius(radius + 10);  // Tăng bán kính để tạo hiệu ứng radar
                        }

                        handler.postDelayed(this, 200);  // Cập nhật mỗi 200ms để tạo hiệu ứng
                    }
                }
            };

            handler.post(runnable);  // Bắt đầu chạy hiệu ứng radar
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null && googleMap != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        // Di chuyển camera đến vị trí người dùng và zoom vào
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));

                        // Thêm marker tại vị trí người dùng
                        googleMap.addMarker(new MarkerOptions()
                                .position(userLocation)
                                .title("Your Location"));

                        // Hiển thị thông tin vị trí
                        showLocationInfo(userLocation);
                    } else {
                        Toast.makeText(requireContext(), "Unable to get location!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void markLocation() {
        if (selectedLocation != null) {
            showMarkerDialog(selectedLocation);
        } else {
            Toast.makeText(requireContext(), "Please select a location on the map!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMarkerDialog(LatLng location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Enter marker content");

        final EditText input = new EditText(requireContext());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String markerText = input.getText().toString().trim();
            if (location != null && !markerText.isEmpty()) {
                // Thêm marker trên bản đồ
                googleMap.addMarker(new MarkerOptions().position(location).title(markerText));

                // Tạo dữ liệu ổ gà
                Pothole pothole = new Pothole(
                        "username", // điền tên người dùng ở đây
                        1.0, // kích thước giả định
                        0.5, // độ sâu giả định
                        0.5, // đường kính giả định
                        markerText, // nhãn ổ gà
                        new Pothole.LocationData(
                                String.valueOf(location.longitude),
                                String.valueOf(location.latitude),
                                "address" // điền địa chỉ nếu có
                        )
                );

                // Gọi API để lưu ổ gà
                apiService.savePothole(pothole).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Marker saved on server!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to save marker!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                selectedLocation = null;
            } else {
                Toast.makeText(requireContext(), "Please enter content!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }
}