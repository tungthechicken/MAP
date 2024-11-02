package com.example.map;

import android.Manifest; // Thư viện cho quyền truy cập
import android.content.pm.PackageManager; // Thư viện cho quản lý gói
import android.os.Bundle; // Thư viện cho Bundle (dùng để truyền dữ liệu)
import android.util.Log;
import android.widget.Button; // Thư viện cho Button
import android.widget.EditText; // Thư viện cho EditText
import android.widget.Toast; // Thư viện cho Toast
import androidx.annotation.NonNull; // Thư viện cho annotation không null
import androidx.appcompat.app.AlertDialog; // Thư viện cho hộp thoại AlertDialog
import androidx.appcompat.app.AppCompatActivity; // Thư viện cho hoạt động AppCompat
import androidx.core.app.ActivityCompat; // Thư viện cho hỗ trợ hoạt động
import com.google.android.gms.location.FusedLocationProviderClient; // Thư viện cho dịch vụ vị trí
import com.google.android.gms.location.LocationServices; // Thư viện cho dịch vụ vị trí
import com.google.android.gms.maps.CameraUpdateFactory; // Thư viện cho cập nhật camera
import com.google.android.gms.maps.GoogleMap; // Thư viện cho GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback; // Thư viện cho callback khi bản đồ sẵn sàng
import com.google.android.gms.maps.SupportMapFragment; // Thư viện cho SupportMapFragment
import com.google.android.gms.maps.model.LatLng; // Thư viện cho tọa độ
import com.google.android.gms.maps.model.MarkerOptions; // Thư viện cho marker trên bản đồ
import android.hardware.Sensor; // Thư viện cho cảm biến
import android.hardware.SensorEvent; // Thư viện cho sự kiện cảm biến
import android.hardware.SensorEventListener; // Thư viện cho người nghe sự kiện cảm biến
import android.hardware.SensorManager; // Thư viện cho quản lý cảm biến
import android.os.Handler; // Thư viện cho xử lý tác vụ
import android.widget.SearchView;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Handler;
import android.Manifest;
import android.content.Intent;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng selectedLocation;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private SensorEventListener sensorEventListener;
    private boolean isShaking = false;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.id_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnShowLocation = findViewById(R.id.btn_show_location);
        btnShowLocation.setOnClickListener(v -> getLocation());

        Button btnAddMarker = findViewById(R.id.btn_add_marker);
        btnAddMarker.setOnClickListener(v -> markLocation());

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                if (Math.sqrt(x * x + y * y + z * z) > 12) {
                    if (!isShaking) {
                        isShaking = true;
                        getLocationForShake();
                    }
                } else {
                    isShaking = false;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Không cần xử lý ở đây
            }
        };

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        SearchView searchView = findViewById(R.id.search_location);
        setupSearchView(searchView);
    }

    private void setupSearchView(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(MapActivity.this);
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
        runOnUiThread(() -> {
            String locationInfo = "Vị trí của bạn: Latitude: " + location.latitude +
                    ", Longitude: " + location.longitude;
            Toast.makeText(this, locationInfo, Toast.LENGTH_LONG).show();
        });
    }

    private void getLocationForShake() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        // Đánh dấu vị trí hiện tại trên bản đồ
                        if (googleMap != null) {
                            googleMap.clear(); // Xóa các marker trước đó nếu muốn chỉ hiện một marker
                            googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Vị trí hiện tại"));

                            // Zoom đến vị trí hiện tại
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }

                        // Hiển thị thông tin vị trí dưới dạng thông báo
                        showLocationInfo(currentLocation);
                    } else {
                        Toast.makeText(this, "Không thể lấy vị trí!", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    googleMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(this, "Lỗi: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(selectedLocation).title("Vị trí đã chọn"));
        });
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null && googleMap != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.addMarker(new MarkerOptions().position(userLocation).title("Bạn đang ở đây"));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    }
                });
    }

    private void markLocation() {
        if (selectedLocation != null) {
            showMarkerDialog(selectedLocation);
        } else {
            Toast.makeText(this, "Vui lòng chọn vị trí trên bản đồ!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMarkerDialog(LatLng location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập nội dung cho đánh dấu");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String markerText = input.getText().toString().trim();
            if (location != null && !markerText.isEmpty()) {
                googleMap.addMarker(new MarkerOptions().position(location).title(markerText));
                Toast.makeText(this, "Đã đánh dấu!", Toast.LENGTH_SHORT).show();
                selectedLocation = null;
            } else {
                Toast.makeText(this, "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

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
