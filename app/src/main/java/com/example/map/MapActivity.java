package com.example.map;

// Các thư viện cần thiết
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
    public GoogleMap googleMap; // Đối tượng GoogleMap
    public FusedLocationProviderClient fusedLocationClient; // Đối tượng để lấy vị trí
    private LatLng selectedLocation; // Biến để lưu vị trí đã chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Thiết lập layout cho Activity
        setContentView(R.layout.activity_map);

        // Khởi tạo FusedLocationProviderClient để lấy vị trí
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Lấy SupportMapFragment và yêu cầu bản đồ sẵn sàng
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.id_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Lấy nút từ layout
        Button btnShowLocation = findViewById(R.id.btn_show_location);
        // Thiết lập sự kiện nhấn nút
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gọi phương thức để lấy vị trí
                getLocation();
            }
        };

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

    private void showAlert() {
        runOnUiThread(() -> {
            Toast toast = Toast.makeText(this, "có ổ gà", Toast.LENGTH_SHORT);
            toast.show();

            // Để ẩn thông báo sau 3 giây
            new Handler().postDelayed(toast::cancel, 3000);
        });
    }

    // Phương thức được gọi khi bản đồ đã sẵn sàng
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        Log.d("MapActivity", "onMapReady");

        // Thiết lập sự kiện nhấp chuột lên bản đồ
        googleMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng; // Lưu vị trí đã chọn
            googleMap.clear(); // Xóa tất cả marker cũ
            googleMap.addMarker(new MarkerOptions().position(selectedLocation).title("Vị trí đã chọn")); // Thêm marker tại vị trí đã chọn
        });
    }

    // Phương thức để lấy vị trí hiện tại
    private void getLocation() {
        // Kiểm tra quyền truy cập vị trí
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa có quyền, yêu cầu quyền truy cập
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return; // Kết thúc phương thức nếu chưa có quyền
        }

        // Lấy vị trí cuối cùng
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Kiểm tra nếu vị trí không null và bản đồ đã được khởi tạo
                    if (location != null && googleMap != null) {
                        // Tạo đối tượng LatLng từ vị trí
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        // Thêm marker tại vị trí của người dùng
                        googleMap.addMarker(new MarkerOptions().position(userLocation).title("Bạn đang ở đây"));
                        // Di chuyển camera đến vị trí của người dùng và phóng to
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    }
                });
    }

    //Phuong thuc danh dau ban do
    private void markLocation() {
        if (selectedLocation != null) {
            showMarkerDialog(selectedLocation); // Hiển thị hộp thoại với vị trí đã chọn
        } else {
            Toast.makeText(this, "Vui lòng chọn vị trí trên bản đồ!", Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức hiển thị hộp thoại để nhập nội dung cho marker !!!!!ERROR
    private void showMarkerDialog(LatLng location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập nội dung cho đánh dấu");

        // Tạo EditText để nhập nội dung
        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String markerText = input.getText().toString();
            if (location != null && !markerText.isEmpty()) {
                // Thêm marker vào vị trí đã chọn
                googleMap.addMarker(new MarkerOptions().position(location).title(markerText));
                Toast.makeText(this, "Đã đánh dấu!", Toast.LENGTH_SHORT).show();
                selectedLocation = null; // Đặt lại selectedLocation để thoát khỏi chế độ đánh dấu
            } else {
                Toast.makeText(this, "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
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
