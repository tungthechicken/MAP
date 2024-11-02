package com.example.map;

// Các thư viện cần thiết
import android.Manifest; // Thư viện cho quyền truy cập
import android.content.pm.PackageManager; // Thư viện cho quản lý gói
import android.location.Location; // Thư viện cho đối tượng Location
import android.os.Bundle; // Thư viện cho Bundle (dùng để truyền dữ liệu)
import android.view.View; // Thư viện cho View
import android.widget.Button; // Thư viện cho Button
import androidx.annotation.NonNull; // Thư viện cho annotation không null
import androidx.appcompat.app.AppCompatActivity; // Thư viện cho hoạt động AppCompat
import androidx.core.app.ActivityCompat; // Thư viện cho hỗ trợ hoạt động
import com.google.android.gms.location.FusedLocationProviderClient; // Thư viện cho dịch vụ vị trí
import com.google.android.gms.location.LocationServices; // Thư viện cho dịch vụ vị trí
import com.google.android.gms.tasks.OnSuccessListener; // Thư viện cho listener khi hoàn thành tác vụ
import com.mapbox.mapboxsdk.Mapbox; // Thư viện cho Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory; // Thư viện cho cập nhật camera
import com.mapbox.mapboxsdk.maps.MapView; // Thư viện cho MapView
import com.mapbox.mapboxsdk.maps.MapboxMap; // Thư viện cho bản đồ
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback; // Thư viện cho callback khi bản đồ sẵn sàng
import com.mapbox.mapboxsdk.maps.Style; // Thư viện cho style bản đồ
import com.mapbox.mapboxsdk.geometry.LatLng; // Thư viện cho tọa độ
import com.mapbox.mapboxsdk.annotations.MarkerOptions; // Thư viện cho marker trên bản đồ

public class MapActivity extends AppCompatActivity {
    // Khai báo các biến
    private MapView mapView; // Đối tượng MapView
    private FusedLocationProviderClient fusedLocationClient; // Đối tượng để lấy vị trí
    private MapboxMap mapboxMap; // Đối tượng MapboxMap

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo Mapbox với context của Activity
        Mapbox.getInstance(this);
        // Thiết lập layout cho Activity
        setContentView(R.layout.activity_map);

        // Lấy MapView từ layout
        mapView = findViewById(R.id.mapView);
        // Khởi động MapView với trạng thái trước đó
        mapView.onCreate(savedInstanceState);

        // Khởi tạo FusedLocationProviderClient để lấy vị trí
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Khi bản đồ đã sẵn sàng
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap map) {
                // Gán đối tượng MapboxMap cho biến mapboxMap
                mapboxMap = map;
                // Thiết lập style cho bản đồ bằng API key của MapTiler
                mapboxMap.setStyle(new Style.Builder().fromUri("https://api.maptiler.com/maps/basic-v2/style.json?key=VGI3lrtwrAjXKYT8kDHE"));
            }
        });

        // Lấy nút từ layout
        Button btnShowLocation = findViewById(R.id.btn_show_location);
        // Thiết lập sự kiện nhấn nút
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gọi phương thức để lấy vị trí
                getLocation();
            }
        });
    }

    // Phương thức để lấy vị trí hiện tại
    private void getLocation() {
        // Kiểm tra quyền truy cập vị trí
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa có quyền, yêu cầu quyền truy cập
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return; // Kết thúc phương thức nếu chưa có quyền
        }

        // Lấy vị trí cuối cùng
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Kiểm tra nếu vị trí không null và bản đồ đã được khởi tạo
                        if (location != null && mapboxMap != null) {
                            // Tạo đối tượng LatLng từ vị trí
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            // Thêm marker tại vị trí của người dùng
                            mapboxMap.addMarker(new MarkerOptions().position(userLocation).title("Bạn đang ở đây"));
                            // Di chuyển camera đến vị trí của người dùng và phóng to
                            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        }
                    }
                });
    }

    // Các phương thức vòng đời của MapView
    @Override
    public void onStart() {
        super.onStart();
        // Gọi phương thức onStart của MapView
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Gọi phương thức onResume của MapView
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Gọi phương thức onPause của MapView
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Gọi phương thức onStop của MapView
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // Gọi phương thức onLowMemory của MapView
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Gọi phương thức onDestroy của MapView
        mapView.onDestroy();
    }

    // Xử lý kết quả của yêu cầu quyền
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            // Nếu quyền đã được cấp
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Gọi lại phương thức lấy vị trí
                getLocation();
            }
        }
    }
}
