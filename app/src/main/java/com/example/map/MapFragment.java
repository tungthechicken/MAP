package com.example.map;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Text;

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
    private static final long SHAKE_INTERVAL = 2000; // Khoảng thời gian (2 giây) giữa các lần lắc

    private OpenRouteServiceAPI openRouteServiceAPI;

    private String name;
    private LatLng destinationLatLng; // Vị trí đích được chọn
    public LatLng userLocation;  // Biến lưu trữ vị trí người dùng
    private Polyline currentRoute;  // Biến để lưu đối tượng đường dẫn
    private List<Marker> potholeMarkers = new ArrayList<>();
    private List<Marker> potholeMarkersRoute = new ArrayList<>();
    //private boolean canSelectLocation = false; // Biến này kiểm tra xem có thể chọn vị trí hay không
    //public boolean canDirection = false;
    private boolean canDetectPothole=false;
    private SearchView searchView;
    private List<Pothole> potholes;
    private boolean canShowPothole = false;
    private List<Marker> markersList = new ArrayList<>();
    float shakeThresholdBig    = 40;
    float shakeThresholdNormal = 35;
    float shakeThresholdSmall  = 30;
    int distancePotholeLimit = 20 ; //20m
    ImageButton btnCancelRoute;
    ImageButton btnEnablePothole;
    ImageButton btnShowLocation;
    ImageButton btnAddPothole;
    private boolean isTrackingUser = false; // Biến flag để kiểm tra nếu đang theo dõi người dùng
    ImageButton btnTracking;
    ImageButton btnHelp;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Retrieve the user's name from the Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            name = bundle.getString("name");
        }
        if (name == null) {
            name = "User"; // Default value if name is null
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        String BASE_URL = getString(R.string.retrofit_url);
        apiService = RetrofitClient.getClient(BASE_URL).create(PotholeApiService.class);


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.id_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        //Button btnGetUserPotholes = view.findViewById(R.id.btn_get_user_potholes);
        //btnGetUserPotholes.setOnClickListener(v -> getPotholesByUsername());

        btnHelp = view.findViewById(R.id.btn_help);
        btnHelp.setOnClickListener(v -> showHelpDialog());

        btnShowLocation = view.findViewById(R.id.btn_show_location);
        btnShowLocation.setOnClickListener(v -> getLocation());

        btnCancelRoute = view.findViewById(R.id.btn_cancel_route);
        btnCancelRoute.setOnClickListener(v -> cancelRoute());
        btnCancelRoute.setVisibility(View.GONE); // Ẩn hoàn toàn nút

        btnTracking = view.findViewById(R.id.btn_tracking);
        btnTracking.setOnClickListener(v -> startTracking());
        btnTracking.setVisibility(View.GONE);

        btnEnablePothole = view.findViewById(R.id.btn_enable_detectpothole);
        btnEnablePothole.setOnClickListener(v -> enableDetectPothole());

        ImageButton btnEnableShowPothole = view.findViewById(R.id.btn_enable_show);
        btnEnableShowPothole.setOnClickListener(v -> EnableShowPothole(btnEnableShowPothole));

        btnAddPothole = view.findViewById(R.id.btn_add_pothole);
        btnAddPothole.setOnClickListener(v -> getLocationForAddPothole());

        // Khởi tạo SearchView
        searchView = view.findViewById(R.id.search_location);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty()) {
                    searchLocation(query);  // Gọi hàm tìm kiếm khi người dùng nhấn Enter
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (!canDetectPothole)
                    {
                        return;
                    }
                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];

                    long currentTime = System.currentTimeMillis(); // Lấy thời gian hiện tại

                    double shakeLevel = Math.sqrt(x * x + y * y + z * z);
                    if (shakeLevel > shakeThresholdSmall && shakeLevel < shakeThresholdNormal) {
                        //lac nhe
                        // Kiểm tra nếu lắc đủ mạnh và đã qua thời gian chờ giữa các lần lắc
                        if (!isShaking && (currentTime - lastShakeTime) > SHAKE_INTERVAL) {
                            isShaking = true;
                            lastShakeTime = currentTime; // Cập nhật thời gian lắc cuối cùng
                            getLocationForShake(1); // Gọi hàm xử lý lắc
                        }
                    }
                    else if (shakeLevel > shakeThresholdNormal && shakeLevel < shakeThresholdBig) {
                        //lac trung binh
                        // Kiểm tra nếu lắc đủ mạnh và đã qua thời gian chờ giữa các lần lắc
                        if (!isShaking && (currentTime - lastShakeTime) > SHAKE_INTERVAL) {
                            isShaking = true;
                            lastShakeTime = currentTime; // Cập nhật thời gian lắc cuối cùng
                            getLocationForShake(2); // Gọi hàm xử lý lắc
                        }
                    }
                    else if (shakeLevel > shakeThresholdBig) {
                        //lac trung manh
                        // Kiểm tra nếu lắc đủ mạnh và đã qua thời gian chờ giữa các lần lắc
                        if (!isShaking && (currentTime - lastShakeTime) > SHAKE_INTERVAL) {
                            isShaking = true;
                            lastShakeTime = currentTime; // Cập nhật thời gian lắc cuối cùng
                            getLocationForShake(3); // Gọi hàm xử lý lắc
                        }
                    }
                    else {
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
        return view;
    }
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);


        // Cấu hình các sự kiện khác cho bản đồ
//        googleMap.setOnMapClickListener(latLng -> {
//            selectedLocation = latLng;
//            googleMap.clear(); // Xóa các marker cũ
//            googleMap.addMarker(new MarkerOptions().position(selectedLocation).title("Selected Location"));
//        });


        //nhan giu de tao marker
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);

        // Sự kiện nhấn giữ trên bản đồ
        googleMap.setOnMapLongClickListener(latLng -> {
            // Tạo marker mới tại vị trí người dùng nhấn giữ
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title("Vị trí đã chọn") // Tiêu đề marker
                    .snippet("Lat: " + latLng.latitude + ", Lng: " + latLng.longitude); // Mô tả

            //xoa marker cu
            removeMarker();

            // Thêm marker vào bản đồ
            Marker newMarker = googleMap.addMarker(markerOptions);
            // Lưu marker vào danh sách
            markersList.add(newMarker);

            // Lưu vị trí đã chọn (nếu cần sử dụng sau này)
            selectedLocation = latLng;

            // Thông báo cho người dùng
            //Toast.makeText(requireContext(), "Đã đánh dấu vị trí: " + latLng, Toast.LENGTH_SHORT).show();
            showChooseDialog();
        });

        googleMap.setOnMarkerClickListener(marker -> {
            // Lấy tọa độ của marker
            LatLng markerPothole = marker.getPosition();
            double latitude = markerPothole.latitude;
            double longitude = markerPothole.longitude;


            // Gọi API để lấy thông tin pothole từ server
            getPotholeDetailsFromApi(latitude, longitude);

            return true; // Trả về true để không hiển thị InfoWindow mặc định
        });

        // Lấy vị trí của người dùng
        googleMap.setOnMyLocationChangeListener(location -> {
            userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        });
    }

    private void getPotholeDetailsFromApi(double latitude, double longitude) {
        // Tạo Retrofit instance
        String BASE_URL = getString(R.string.retrofit_url);
        PotholeApiService apiService = RetrofitClient.getClient(BASE_URL).create(PotholeApiService.class);


        // Gọi API để lấy thông tin pothole dựa trên tọa độ
        Call<List<Pothole>> call = apiService.getPotholeByLocation(latitude, longitude);

        call.enqueue(new Callback<List<Pothole>>() {
            @Override
            public void onResponse(Call<List<Pothole>> call, Response<List<Pothole>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pothole> potholes = response.body();
                    // Xử lý và hiển thị thông tin pothole tại đây
                    showPotholeDetailsDialog(potholes,latitude,longitude); // Ví dụ hiển thị tất cả pothole
                } else {
                    // Xử lý khi có lỗi trong phản hồi từ server
                    int statusCode = response.code(); // Lấy mã trạng thái HTTP
                    String errorMessage = "Lỗi không xác định";

                    // Kiểm tra mã trạng thái và hiển thị thông báo lỗi phù hợp
                    switch (statusCode) {
                        case 400:
                            errorMessage = "Yêu cầu không hợp lệ (Bad Request)";
                            break;
                        case 404:
                            errorMessage = "Không tìm thấy pothole";
                            break;
                        case 500:
                            errorMessage = "Lỗi từ server (Internal Server Error)";
                            break;
                        default:
                            errorMessage = "Lỗi mạng hoặc kết nối";
                            break;
                    }
                    // Hiển thị thông báo lỗi cho người dùng
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Pothole>> call2, Throwable t) {
                // Xử lý lỗi khi không thể kết nối với API
                Toast.makeText(getContext(), "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void showPotholeDetailsDialog(List<Pothole> potholes,double latitude, double longitude) {
        // Nạp layout tùy chỉnh
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.show_pothole_dialog, null);

        // Lấy các view từ layout
        TextView editLabel = dialogView.findViewById(R.id.editLabel);
        TextView editDepth = dialogView.findViewById(R.id.editDepth);
        TextView editDiameter = dialogView.findViewById(R.id.editDiameter);
        CheckBox cbBig = dialogView.findViewById(R.id.cbBig);
        CheckBox cbNormal = dialogView.findViewById(R.id.cbNormal);
        CheckBox cbSmall = dialogView.findViewById(R.id.cbSmall);
        Button buttonok = dialogView.findViewById(R.id.confirm_cf_add_pothole);
        Button deletePothole = dialogView.findViewById(R.id.delete_pothole);

        // Điền dữ liệu vào các trường trong hộp thoại cho mỗi pothole
        if (!potholes.isEmpty()) {
            Pothole pothole = potholes.get(0); // Giả sử bạn chỉ hiển thị thông tin của pothole đầu tiên trong danh sách
            editLabel.setText(pothole.getStatement()); // Ví dụ
            editDepth.setText(String.valueOf(pothole.getDepth()));
            editDiameter.setText(String.valueOf(pothole.getDiameter()));
            // Cập nhật các checkbox nếu cần (dựa trên thông tin của pothole)
            if (pothole.getSize() == 3) {
                cbBig.setChecked(true);
            } else if (pothole.getSize() == 2) {
                cbNormal.setChecked(true);
            } else if (pothole.getSize() == 1) {
                cbSmall.setChecked(true);
            }
        }

        // Tạo AlertDialog với layout tùy chỉnh
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        // Tạo và hiển thị hộp thoại
        AlertDialog dialog = builder.create();

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.TOP);
        dialog.show();

        // Xử lý khi nhấn nút OK
        buttonok.setOnClickListener(v -> {
            // Lấy dữ liệu từ các trường trong hộp thoại nếu cần
            String label = editLabel.getText().toString();
            String depth = editDepth.getText().toString();
            String diameter = editDiameter.getText().toString();

            // Cập nhật hoặc lưu lại thông tin pothole mới nếu cần

            // Đóng hộp thoại sau khi nhấn OK
            dialog.dismiss(); // Đóng hộp thoại
        });

        deletePothole.setOnClickListener(v -> {
            // Thay thế bằng thông tin thực tế từ người dùng
            //String username = getUsername();
            dialog.dismiss(); // Đóng hộp thoại
            deletePothole(getUsername(),latitude, longitude );

        });
    }

    private void deletePothole(String username, double latitude, double longitude) {
        String latitudeStr = String.valueOf(latitude);
        String longitudeStr = String.valueOf(longitude);

        apiService.deletePothole(username, latitudeStr, longitudeStr)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Log.d("DELETE_POTHOLE", "Request URL: " + call.request().url());
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Pothole deleted successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete pothole: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("DELETE_POTHOLE", "Error: " + t.getMessage());
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Hàm clear chỉ xóa marker đã chọn
    public void clearSelectedMarker(Marker selectedMarker) {
        // Xóa marker đã chọn khỏi bản đồ
        if (selectedMarker != null) {
            selectedMarker.remove(); // Xóa marker
            markersList.remove(selectedMarker); // Xóa khỏi danh sách
        }
    }

    // Hàm hiển thị các ổ gà trên bản đồ
    private void EnableShowPothole(ImageButton btnEnableShowPothole) {
        canShowPothole=!canShowPothole;

        if (canShowPothole)
        {
            //Toast.makeText(requireContext(), "enable show pothole!", Toast.LENGTH_SHORT).show();
            btnEnableShowPothole.setImageResource(R.drawable.pothole_marker_hide);
            callPotholes();

        }
        else
        {
            //Toast.makeText(requireContext(), "disable show pothole!", Toast.LENGTH_SHORT).show();
            btnEnableShowPothole.setImageResource(R.drawable.pothole_marker);
            clearPotholes();
        }
    }
    private void callPotholes() {
        // Khởi tạo Retrofit
        String BASE_URL = getString(R.string.retrofit_url);
        PotholeApiService apiService = RetrofitClient.getClient(BASE_URL).create(PotholeApiService.class);

        // Tạo đối tượng apiService
        Call<List<Pothole>> call = apiService.getPotholes();
        // Gọi API để lấy danh sách ổ gà
        call.enqueue(new Callback<List<Pothole>>() {
            @Override
            public void onResponse(Call<List<Pothole>> call, Response<List<Pothole>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pothole> potholes = response.body();
                    // Gọi hàm để hiển thị các pothole trên bản đồ
                    showPotholesOnMap(potholes);
                }
            }
            @Override
            public void onFailure(Call<List<Pothole>> call, Throwable t) {
                // Xử lý lỗi nếu có
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showPotholesOnMap(List<Pothole> potholes) {
        for (Pothole pothole : potholes) {
            LatLng location = new LatLng(
                    Double.parseDouble(pothole.getLocation().getLatitude()),
                    Double.parseDouble(pothole.getLocation().getLongitude())
            );

            // Tạo MarkerOptions để định nghĩa thông tin cho marker
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location)
                    .title(pothole.getStatement());

            // Lấy tài nguyên biểu tượng marker và thay đổi kích thước
            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pothole_marker);
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            int newWidth = (int) (width * 0.5);  // Giảm 50% kích thước gốc
            int newHeight = (int) (height * 0.5);  // Giảm 50% kích thước gốc

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false);

            // Sử dụng Bitmap đã thay đổi kích thước làm biểu tượng cho marker
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));

            // Thêm marker vào bản đồ và lưu vào danh sách potholeMarkers
            Marker potholeMarker = googleMap.addMarker(markerOptions);
            potholeMarkers.add(potholeMarker);
        }
    }
    private void clearPotholes() {
        // Duyệt qua danh sách potholeMarkers và xóa tất cả các marker
        for (Marker potholeMarker : potholeMarkers) {
            if (potholeMarker != null) {
                potholeMarker.remove();  // Xóa marker
            }
        }
        potholeMarkers.clear();  // Xóa tất cả các phần tử trong danh sách potholeMarkers
    }
    // Hàm hiển thị các ổ gà trên bản đồ

    private void searchLocation(String query) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.openstreetmap_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NominatimService service = retrofit.create(NominatimService.class);
        Call<List<NominatimResult>> call = service.searchLocation(query, "json");

        call.enqueue(new Callback<List<NominatimResult>>() {
            @Override
            public void onResponse(Call<List<NominatimResult>> call, Response<List<NominatimResult>> response) {
                if (response.isSuccessful()) {
                    List<NominatimResult> results = response.body();
                    if (results != null && !results.isEmpty()) {
                        NominatimResult result = results.get(0);
                        double lat = Double.parseDouble(result.getLat());
                        double lon = Double.parseDouble(result.getLon());
                        LatLng location = new LatLng(lat, lon);

                        // Di chuyển bản đồ đến vị trí mới và thêm marker
                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions().position(location).title(result.getDisplay_name()));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                    } else {
                        Toast.makeText(requireContext(), "Không tìm thấy địa điểm", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<NominatimResult>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


//them o ga lac dt
    private void getLocationForShake(double size) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        MarkerOptions markerOptions = null;
                        if (googleMap != null) {
                            //googleMap.clear();
                            Marker newMarker = googleMap.addMarker(
                                    new MarkerOptions()
                                            .position(currentLocation) // Thiết lập vị trí marker
                                            .title("Current Location")
                            );
                            markersList.add(newMarker);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        }
                        //showLocationInfo(currentLocation); //hien thi thong tin vi tri cua nguoi dung khi lac
                        // Hiển thị hộp thoại xác nhận
                        showConfirmationDialog(currentLocation, size);

                        // Gọi phương thức để phát âm thanh hoặc rung tùy vào chế độ âm thanh
                        playSoundOrVibrate(R.raw.notification);  // Phát âm thanh hoặc rung
                    } else {
                        Toast.makeText(requireContext(), "Unable to get location!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showConfirmationDialog(LatLng location, double size) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
        View mView = getLayoutInflater().inflate(R.layout.confirm_pothole_dialog, null);
        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCancelable(false);

        // Tham chiếu tới các view trong layout
        CheckBox cbBig = mView.findViewById(R.id.cbBig);
        CheckBox cbNormal = mView.findViewById(R.id.cbNormal);
        CheckBox cbSmall = mView.findViewById(R.id.cbSmall);
        EditText editDepth = mView.findViewById(R.id.editDepth);
        EditText editDiameter = mView.findViewById(R.id.editDiameter);
        EditText editLabel = mView.findViewById(R.id.editLabel);

        // Đánh dấu CheckBox dựa trên giá trị size
        if (size == 3) {
            cbBig.setChecked(true);
        } else if (size == 2) {
            cbNormal.setChecked(true);
        } else if (size == 1) {
            cbSmall.setChecked(true);
        }

        // Xử lý CheckBox: Chỉ chọn một CheckBox tại một thời điểm
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (isChecked) {
                if (buttonView != cbBig) cbBig.setChecked(false);
                if (buttonView != cbNormal) cbNormal.setChecked(false);
                if (buttonView != cbSmall) cbSmall.setChecked(false);
            }
        };
        cbBig.setOnCheckedChangeListener(listener);
        cbNormal.setOnCheckedChangeListener(listener);
        cbSmall.setOnCheckedChangeListener(listener);

        // Tự động điền giá trị "1234" vào các trường EditText
        editDepth.setText("0");
        editDiameter.setText("0");
        editLabel.setText("unknown");

        // Xử lý sự kiện khi nhấn nút Cancel
        mView.findViewById(R.id.cancel_cf_pothole).setOnClickListener(v -> {
            removeMarker();
            alertDialog.dismiss();
        });

        // Xử lý sự kiện khi nhấn nút Confirm
        mView.findViewById(R.id.confirm_cf_pothole).setOnClickListener(v -> {
            removeMarker();
            savePothole(location, editLabel.getText().toString(), size, Double.parseDouble(editDepth.getText().toString()), Double.parseDouble(editDiameter.getText().toString()));
            alertDialog.dismiss();
        });

        // Đặt hộp thoại hiển thị ở phía dưới
        alertDialog.getWindow().setGravity(Gravity.BOTTOM);
        alertDialog.show();
    }

    private void savePothole(LatLng location, String label, double size, double depth, double diameter) {
        // Tạo dữ liệu ổ gà
        Pothole pothole = new Pothole(
                getUsername(), // Include the user's name here
                size, // kích thước giả định
                depth, // độ sâu giả định
                diameter, // đường kính giả định
                label, // nhãn ổ gà
                new Pothole.LocationData(
                        String.valueOf(location.longitude),
                        String.valueOf(location.latitude),
                        "address" // điền địa chỉ nếu có
                ),
                getCurrentDate() // Lưu ngày tháng năm vào Pothole
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
//them o ga lac dt
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


    //nhan giu de hien lua chon
    private void showChooseDialog() {

        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_dialog);

        ImageButton btn_direct = dialog.findViewById(R.id.button_direct);
        ImageButton btn_cancel = dialog.findViewById(R.id.button_cancel);
        ImageButton btn_add_hole = dialog.findViewById(R.id.button_add_pothole);

        btn_direct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //Toast.makeText(MapFragment.this,"Edit is Clicked",Toast.LENGTH_SHORT).show();
                //getRoute(userLocation, selectedLocation);
                enableDirection();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //Toast.makeText(MainActivity.this,"Share is Clicked",Toast.LENGTH_SHORT).show();
                //cancelRoute();
                removeMarker();

            }
        });

        btn_add_hole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                //Toast.makeText(MainActivity.this,"Upload is Clicked",Toast.LENGTH_SHORT).show();
                addPotHole();

            }
        });
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }
//nhan giu de hien lua chon

    private void enableDetectPothole() {
        canDetectPothole = !canDetectPothole;
        if(canDetectPothole)
        {
            Toast.makeText(requireContext(), "enable POTHOLE detect", Toast.LENGTH_SHORT).show();
            enablePotholeDetectBtn();
        }
        else
        {

            Toast.makeText(requireContext(), "disable POTHOLE detect", Toast.LENGTH_SHORT).show();
            disablePotholeDetectBtn();
        }

    }


// chuc nang dan duong
    private  void startTracking()
    {
        isTrackingUser=!isTrackingUser;

        if (isTrackingUser)
        {
            Toast.makeText(getContext(), "Started tracking on road ", Toast.LENGTH_SHORT).show();
            btnTracking.setImageResource(R.drawable.cancel_tracking);
        }
        else
        {
            Toast.makeText(getContext(), "Canceled tracking on road ", Toast.LENGTH_SHORT).show();
            btnTracking.setImageResource(R.drawable.tracking);
        }

    }
    private void enableDirection() {
            //Toast.makeText(requireContext(), "Please, select location!", Toast.LENGTH_SHORT).show();
        enableRouteCancelButton();
        enableTrackingButton();
        getDirection();
    }
    private void getDirection() {

                //googleMap.addMarker(new MarkerOptions().position(selectedLocation).title("Selected Location"));
                if (userLocation != null) {
                    // Gọi API để lấy chỉ đường từ vị trí người dùng đến điểm đã chọn
                    getRoute(userLocation, selectedLocation);
                }
    }
    private void getRoute(LatLng startLatLng, LatLng endLatLng) {
        if (endLatLng == null) return;

        String start = startLatLng.longitude + "," + startLatLng.latitude;
        String end = endLatLng.longitude + "," + endLatLng.latitude;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.openrouteservice_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OpenRouteServiceAPI api = retrofit.create(OpenRouteServiceAPI.class);
        Call<RouteResponse> call = api.getRoute(getString(R.string.openrouteservice_key), start, end);

        call.enqueue(new Callback<RouteResponse>() {
            @Override
            public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                if (response.isSuccessful()) {
                    RouteResponse routeResponse = response.body();
                    if (routeResponse != null && routeResponse.getFeatures() != null) {
                        List<LatLng> route = new ArrayList<>();
                        List<LatLng> waypoints = new ArrayList<>();

                        // Trích xuất các waypoint
                        for (double[] coord : routeResponse.getFeatures()[0].getGeometry().getCoordinates()) {
                            LatLng point = new LatLng(coord[1], coord[0]);
                            route.add(point);
                            waypoints.add(point);
                        }

                        // Vẽ đường trên bản đồ
                        if (googleMap != null) {
                            PolylineOptions polylineOptions = new PolylineOptions()
                                    .addAll(route)
                                    .color(Color.BLUE)
                                    .width(22);
                            currentRoute = googleMap.addPolyline(polylineOptions);
                        }

                        // Lấy danh sách pothole từ server
                        getPotholesFromServer(new PotholeDataCallback() {
                            @Override
                            public void onPotholesRetrieved(List<Pothole> potholes) {
                                detectPotholesOnRoute(waypoints, potholes);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });


                        // Bắt đầu theo dõi vị trí
                        startNavigation(endLatLng);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to get route", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RouteResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getPotholesFromServer(final PotholeDataCallback callback) {
        // Khởi tạo Retrofit
        String BASE_URL = getString(R.string.retrofit_url);
        PotholeApiService apiService = RetrofitClient.getClient(BASE_URL).create(PotholeApiService.class);

        // Gọi API để lấy danh sách ổ gà
        Call<List<Pothole>> call = apiService.getPotholes();
        call.enqueue(new Callback<List<Pothole>>() {
            @Override
            public void onResponse(Call<List<Pothole>> call, Response<List<Pothole>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pothole> potholes = response.body();
                    List<LatLng> potholeLocations = new ArrayList<>();

                    // Chuyển đổi thông tin từ Pothole thành LatLng
                    for (Pothole pothole : potholes) {
                        double latitude = Double.parseDouble(pothole.getLocation().getLatitude());
                        double longitude = Double.parseDouble(pothole.getLocation().getLongitude());
                        potholeLocations.add(new LatLng(latitude, longitude));
                    }

                    // Gọi callback để trả về kết quả
                    callback.onPotholesRetrieved(potholes); // Trả về danh sách Pothole

                } else {
                    // Gọi callback nếu không thành công
                    callback.onError("Failed to fetch potholes.");
                }
            }

            @Override
            public void onFailure(Call<List<Pothole>> call, Throwable t) {
                // Gọi callback khi có lỗi kết nối
                callback.onError("Error: " + t.getMessage());
            }
        });
    }
    private boolean isPotholeNearLine(LatLng pointA, LatLng pointB, LatLng pothole) {
        // Công thức Haversine để tính khoảng cách giữa 2 điểm trên mặt đất
        double distanceThreshold = 5.0; // Ngưỡng khoảng cách tính bằng mét, ví dụ 50m

        // Khoảng cách giữa 2 điểm waypoint A và B
        double segmentDistance = haversine(pointA, pointB);

        // Tính khoảng cách từ Pothole đến đoạn thẳng AB
        double potholeDistance = distanceToLineSegment(pointA, pointB, pothole);

        return potholeDistance <= distanceThreshold;
    }
    // Hàm Haversine để tính khoảng cách giữa 2 điểm trên bề mặt Trái Đất
    private double haversine(LatLng point1, LatLng point2) {
        final double R = 6371000; // Bán kính Trái Đất (đơn vị: mét)

        double lat1 = Math.toRadians(point1.latitude);
        double lon1 = Math.toRadians(point1.longitude);
        double lat2 = Math.toRadians(point2.latitude);
        double lon2 = Math.toRadians(point2.longitude);

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Khoảng cách trong mét
    }
    // Hàm tính khoảng cách từ điểm đến đoạn thẳng
    private double distanceToLineSegment(LatLng pointA, LatLng pointB, LatLng point) {
        double x1 = pointA.longitude, y1 = pointA.latitude;
        double x2 = pointB.longitude, y2 = pointB.latitude;
        double x0 = point.longitude, y0 = point.latitude;

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dot = (x0 - x1) * dx + (y0 - y1) * dy;
        double len_sq = dx * dx + dy * dy;
        double param = -1.0;

        if (len_sq != 0.0) { // Tránh chia cho 0
            param = dot / len_sq;
        }

        double xx, yy;

        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * dx;
            yy = y1 + param * dy;
        }

        // Tính khoảng cách từ điểm P đến đoạn thẳng
        return haversine(new LatLng(y0, x0), new LatLng(yy, xx));
    }
    private void detectPotholesOnRoute(List<LatLng> waypoints, List<Pothole> potholes) {
        for (int i = 0; i < waypoints.size() - 1; i++) {
            LatLng pointA = waypoints.get(i);
            LatLng pointB = waypoints.get(i + 1);

            for (Pothole pothole : potholes) {
                double latitude = Double.parseDouble(pothole.getLocation().getLatitude());
                double longitude = Double.parseDouble(pothole.getLocation().getLongitude());
                LatLng potholeLocation = new LatLng(latitude, longitude);

                if (isPotholeNearLine(pointA, pointB, potholeLocation)) {
                    // Chọn icon dựa theo size của pothole
                    int iconResource = getIconForPotholeSize(pothole.getSize());

                    // Thêm marker với icon thích hợp
                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(potholeLocation)
                            .icon(BitmapDescriptorFactory.fromResource(iconResource))
                            .title("Pothole detected!")
                            .snippet("Size: " + pothole.getSize() + " | Depth: " + pothole.getDepth()));

                    potholeMarkersRoute.add(marker);
                }
            }
        }
    }
    private int getIconForPotholeSize(double size) {
        // Gọi phương thức để phát âm thanh hoặc rung tùy vào chế độ âm thanh
        playSoundOrVibrate(R.raw.notification);  // Phát âm thanh hoặc rung
        if (size >= 3) {
            return R.drawable.warningbig; // Icon cho ổ gà lớn
        } else if (size == 2) {
            return R.drawable.warningnormal; // Icon cho ổ gà trung bình
        } else {
            return R.drawable.warningsmall; // Icon cho ổ gà nhỏ
        }
    }
    private void removePotholeMarkers() {
        for (Marker marker : potholeMarkersRoute) {
            marker.remove(); // Xóa marker khỏi bản đồ
        }
        potholeMarkersRoute.clear(); // Xóa danh sách
    }
    private void startNavigation(LatLng destination) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.requestLocationUpdates(
                LocationRequest.create().setInterval(1000).setFastestInterval(500).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null || googleMap == null ) return;

                        Location userLocation = locationResult.getLastLocation();
                        LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

                        // Di chuyển camera theo dõi người dùng
                        if(isTrackingUser)
                        {
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17));
                        }


                        // Kiểm tra nếu người dùng đến đích
                        if (isUserAtDestination(userLatLng, destination)) {
                            fusedLocationClient.removeLocationUpdates(this); // Dừng theo dõi vị trí
                            Toast.makeText(requireContext(), "You have arrived at your destination!", Toast.LENGTH_SHORT).show();
                            //isTrackingUser=false;
                        }

                        // Kiểm tra các ổ gà gần đó
                        for (Marker potholeMarker : potholeMarkersRoute) {
                            if (isNearPotholeUser(userLatLng, potholeMarker, distancePotholeLimit)) {
                                Toast.makeText(requireContext(), "Warning: Pothole ahead!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                },
                Looper.getMainLooper()
        );
    }

    private boolean isNearPotholeUser(LatLng userLocation, Marker potholecheck, double threshold) {
        LatLng potholePosition = potholecheck.getPosition(); // Lấy vị trí của marker
        double distance = haversine(userLocation, potholePosition); // Tính khoảng cách
        return distance <= threshold; // Kiểm tra nếu khoảng cách <= ngưỡng
    }

    private boolean isUserAtDestination(LatLng userLocation, LatLng destination) {
        float[] results = new float[1];
        Location.distanceBetween(
                userLocation.latitude, userLocation.longitude,
                destination.latitude, destination.longitude,
                results
        );
        return results[0] < 20; // Người dùng được xem như đã đến đích nếu cách điểm đến < 20m
    }
    private void cancelRoute() {
        if (currentRoute != null) {
            currentRoute.remove();  // Xóa đường dẫn trên bản đồ
            currentRoute = null;  // Đặt lại biến currentRoute
            removeMarker();
            removePotholeMarkers();
            disableTrackingButton();
            if (isTrackingUser=true)
            {
                btnTracking.setImageResource(R.drawable.tracking);
                isTrackingUser=false;
            }
            else
                isTrackingUser=false;

            //potholes.clear();
            //googleMap.setOnMapClickListener(null);
            Toast.makeText(requireContext(), "Route canceled", Toast.LENGTH_SHORT).show();

            //isTrackingUser = false; // Tắt theo dõi người dùng khi hủy tuyến đường
        } else {
            Toast.makeText(requireContext(), "No route to cancel", Toast.LENGTH_SHORT).show();
        }
        disableRouteCancelButton();
    }

// chuc nang dan duong

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null && googleMap != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                        btnShowLocation.setImageResource(R.drawable.locate);
                        // Move camera to user location and zoom in
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17));

                        // Add marker at user location
                        //googleMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));

                        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            googleMap.setMyLocationEnabled(true);
                            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                        }
                        // Show location info
                        //showLocationInfo(userLocation);
                    } else {
                        Toast.makeText(requireContext(), "Unable to get location!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to get location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

//them pot hole bang tay
    private void addPotHole() {
        showPotHoleDialog(selectedLocation);

            //googleMap.clear(); // Xóa tất cả các marker hiện tại
            // Đảm bảo không xóa các marker pothole đã được lưu
//            for (Marker potholeMarker : potholeMarkers) {
//                potholeMarker.setVisible(true); // Làm cho các marker pothole hiển thị lại
//            }
    }
    private void playSoundOrVibrate(int soundResId) {
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode(); // Lấy chế độ âm thanh của hệ thống

        if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            // Chế độ bình thường: Phát âm thanh
            MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), soundResId);
            if (mediaPlayer != null) {
                mediaPlayer.start();  // Phát âm thanh
            }
        } else if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            // Chế độ rung: Rung điện thoại
            Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                long[] vibrationPattern = {0, 500, 200, 500};  // Rung 500ms, dừng 200ms, rung lại 500ms
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1));  // Tạo hiệu ứng rung
            }
        }
    }
    private void showPotHoleDialog(LatLng location) {
        // Inflate layout tùy chỉnh
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.add_pothole_dialog, null);

        // Ánh xạ các view trong layout
        EditText labelInput = dialogView.findViewById(R.id.editLabel);
        EditText depthInput = dialogView.findViewById(R.id.editDepth);
        EditText diameterInput = dialogView.findViewById(R.id.editDiameter);
        CheckBox bigCheckBox = dialogView.findViewById(R.id.cbBig); // size=3
        CheckBox normalCheckBox = dialogView.findViewById(R.id.cbNormal); // size=2
        CheckBox smallCheckBox = dialogView.findViewById(R.id.cbSmall); // size=1
        Button cancelButton = dialogView.findViewById(R.id.cancel_cf_add_pothole);
        Button confirmButton = dialogView.findViewById(R.id.confirm_cf_add_pothole);

        // Xử lý CheckBox: Chỉ chọn một CheckBox tại một thời điểm
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (isChecked) {
                if (buttonView != bigCheckBox) bigCheckBox.setChecked(false);
                if (buttonView != normalCheckBox) normalCheckBox.setChecked(false);
                if (buttonView != smallCheckBox) smallCheckBox.setChecked(false);
            }
        };
        bigCheckBox.setOnCheckedChangeListener(listener);
        normalCheckBox.setOnCheckedChangeListener(listener);
        smallCheckBox.setOnCheckedChangeListener(listener);
        // Tạo AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Gắn sự kiện cho nút Confirm
        confirmButton.setOnClickListener(v -> {
            String label = labelInput.getText().toString().trim();
            String depth = depthInput.getText().toString().trim();
            String diameter = diameterInput.getText().toString().trim();
            String size = bigCheckBox.isChecked() ? "Big" :
                    normalCheckBox.isChecked() ? "Normal" : "Small";

            if (!label.isEmpty() && !depth.isEmpty() && !diameter.isEmpty() && location != null) {
                // Lấy ngày hiện tại
                String currentDate = getCurrentDate();

                // Tạo dữ liệu ổ gà
                Pothole pothole = new Pothole(
                        getUsername(),
                        Double.parseDouble(size.equals("Big") ? "3.0" : size.equals("Normal") ? "2.0" : "1.0"), // Giá trị kích thước giả định
                        Double.parseDouble(depth),
                        Double.parseDouble(diameter),
                        label,
                        new Pothole.LocationData(
                                String.valueOf(location.longitude),
                                String.valueOf(location.latitude),
                                "address" // Thay bằng địa chỉ nếu cần
                        ),
                        getCurrentDate()
                );


                // Gọi API để lưu ổ gà
                apiService.savePothole(pothole).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Pothole saved successfully!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss(); // Đóng hộp thoại sau khi lưu thành công
                            removeMarker();
                        } else {
                            Toast.makeText(requireContext(), "Failed to save pothole.", Toast.LENGTH_SHORT).show();
                            removeMarker();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(requireContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show();
            }
        });

        // Gắn sự kiện cho nút Cancel
        cancelButton.setOnClickListener(v ->
                {
                    dialog.dismiss();
                    removeMarker();
                }
        );

        // Hiển thị hộp thoại
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.TOP);
    }
    //them pot hole bang tay

    private void getLocationForAddPothole()
    {
        showPotHoleDialog(userLocation);
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
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  // Định dạng ngày tháng
        Date date = new Date();  // Lấy thời gian hiện tại
        return dateFormat.format(date);  // Chuyển đổi thành chuỗi
    }
    private void showLocationInfo(LatLng location) {
        requireActivity().runOnUiThread(() -> {
            String locationInfo = "Your location: Latitude: " + location.latitude +
                    ", Longitude: " + location.longitude;
            Toast.makeText(requireContext(), locationInfo, Toast.LENGTH_LONG).show();
        });
    }
    private void removeMarker() {
        if (!markersList.isEmpty()) {
            // Giả sử bạn có logic để chọn một marker cụ thể để xóa
            clearSelectedMarker(markersList.get(0));  // Xóa marker đầu tiên trong danh sách
        }
    }
    private String  getUsername() {
        return name;
    }

    private void enableRouteCancelButton()
    {
        btnCancelRoute.setVisibility(View.VISIBLE); // Hiển thị lại nút
    }
    private void disableRouteCancelButton()
    {
        btnCancelRoute.setVisibility(View.GONE); // Ẩn hoàn toàn nút và không chiếm không gian
    }
    private void enableTrackingButton()
    {
        btnTracking.setVisibility(View.VISIBLE); // Hiển thị lại nút
    }
    private void disableTrackingButton()
    {
        btnTracking.setVisibility(View.GONE); // Ẩn hoàn toàn nút và không chiếm không gian
    }

    private void enablePotholeDetectBtn()
    {
        btnEnablePothole.setImageResource(R.drawable.motorcycle);
    }
    private void disablePotholeDetectBtn()
    {
        btnEnablePothole.setImageResource(R.drawable.pothole_enable); // Ẩn hoàn toàn nút và không chiếm không gian
    }

    //lam mau de check xem lay duoc pothole theo username de Thu lam Dashboard
    //nho tung tao ham get username:
//    private String  getUsername() {
//        return name;
//    }
    //lay 2 ham ben duoi de lay pothole ve lam dashboard
    private void getPotholesByUsername() {
        // Lấy username
        String username = getUsername();

        // Kiểm tra username hợp lệ
        if (username == null || username.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy username" , Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo Retrofit instance
        String BASE_URL = getString(R.string.retrofit_url);
        PotholeApiService apiService = RetrofitClient.getClient(BASE_URL).create(PotholeApiService.class);

        // Gọi API
        Call<List<Pothole>> call = apiService.getPotholeByUsername(username);

        call.enqueue(new Callback<List<Pothole>>() {
            @Override
            public void onResponse(Call<List<Pothole>> call, Response<List<Pothole>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pothole> potholeList = response.body();
                    showPotholeListDialog(potholeList);
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy ổ gà cho người dùng: " + username, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Pothole>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi khi tải dữ liệu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showPotholeListDialog(List<Pothole> potholeList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Danh sách ổ gà");

        // Tạo danh sách để hiển thị
        StringBuilder message = new StringBuilder();
        for (Pothole pothole : potholeList) {
            message.append("Vị trí: ")
                    .append("Lat: ").append(pothole.getLocation().getLatitude())
                    .append(", Lng: ").append(pothole.getLocation().getLongitude())
                    .append("\nKích thước: ").append(pothole.getSize())
                    .append(", Độ sâu: ").append(pothole.getDepth())
                    .append("\nNgày: ").append(pothole.getDate())
                    .append("\n\n");
        }

        // Hiển thị danh sách trong Dialog
        builder.setMessage(message.toString());
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
    //lam mau de check xem lay duoc pothole theo username de Thu lam Dashboard


    // Hàm hiển thị hướng dẫn sử dụng
    private void showHelpDialog() {
        IntroMap1DialogFragment introMap1DialogFragment = new IntroMap1DialogFragment();
        introMap1DialogFragment.show(getChildFragmentManager(), "IntroMap1Dialog");
    }
}

