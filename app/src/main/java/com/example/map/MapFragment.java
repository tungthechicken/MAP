package com.example.map;

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
import android.media.Image;
import android.os.Bundle;
import android.os.Looper;
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
    private static final long SHAKE_INTERVAL = 1000; // Khoảng thời gian (1 giây) giữa các lần lắc

    private OpenRouteServiceAPI openRouteServiceAPI;

    private LatLng destinationLatLng; // Vị trí đích được chọn
    public LatLng userLocation;  // Biến lưu trữ vị trí người dùng
    private Polyline currentRoute;  // Biến để lưu đối tượng đường dẫn
    private List<Marker> potholeMarkers = new ArrayList<>();
    private boolean canSelectLocation = false; // Biến này kiểm tra xem có thể chọn vị trí hay không
    public boolean canDirection = false;
    private boolean canDetectPothole=false;
    private SearchView searchView;
    private boolean arePotholesVisible = true;
    private List<Pothole> potholes;
    private boolean canShowPothole = false;
    private List<Marker> markersList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        apiService = RetrofitClient.getInstance().create(PotholeApiService.class);


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.id_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ImageButton btnShowLocation = view.findViewById(R.id.btn_show_location);
        btnShowLocation.setOnClickListener(v -> getLocation());
//
//        ImageButton btnAddMarker = view.findViewById(R.id.btn_add_marker);
//        btnAddMarker.setOnClickListener(v -> enableLocationSelection());
//
//        ImageButton btnGetRoute = view.findViewById(R.id.btn_get_direction);
//        btnGetRoute.setOnClickListener(v -> enableDirection(btnGetRoute));
//
        ImageButton btnCancelRoute = view.findViewById(R.id.btn_cancel_route);
        btnCancelRoute.setOnClickListener(v -> cancelRoute());

        ImageButton btnEnablePothole = view.findViewById(R.id.btn_enable_detectpothole);
        btnEnablePothole.setOnClickListener(v -> enableDetectPothole());

        ImageButton btnEnableShowPothole = view.findViewById(R.id.btn_enable_show);
        btnEnableShowPothole.setOnClickListener(v -> EnableShowPothole(btnEnableShowPothole));

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
                    float shakeThreshold = 18; // Ngưỡng lắc (bạn có thể điều chỉnh)

                    long currentTime = System.currentTimeMillis(); // Lấy thời gian hiện tại

                    if (Math.sqrt(x * x + y * y + z * z) > shakeThreshold) {
                        // Kiểm tra nếu lắc đủ mạnh và đã qua thời gian chờ giữa các lần lắc
                        if (!isShaking && (currentTime - lastShakeTime) > SHAKE_INTERVAL) {
                            isShaking = true;
                            lastShakeTime = currentTime; // Cập nhật thời gian lắc cuối cùng
                            getLocationForShake(); // Gọi hàm xử lý lắc
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
        apiService = RetrofitClient.getInstance().create(PotholeApiService.class);

        // Gọi API để lấy thông tin pothole dựa trên tọa độ
        Call<List<Pothole>> call = apiService.getPotholeByLocation(latitude, longitude);

        call.enqueue(new Callback<List<Pothole>>() {
            @Override
            public void onResponse(Call<List<Pothole>> call, Response<List<Pothole>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Pothole> potholes = response.body();
                    // Xử lý và hiển thị thông tin pothole tại đây
                    showPotholeDetailsDialog2(potholes); // Ví dụ hiển thị tất cả pothole
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
    public void showPotholeDetailsDialog(List<Pothole> potholes) {
        // Xử lý hiển thị danh sách các pothole
        StringBuilder details = new StringBuilder();
        for (Pothole pothole : potholes) {
            details.append("Pothole username: ").append(pothole.getUsername()).append("\n");
            details.append("Pothole label: ").append(pothole.getStatement()).append("\n");
            details.append("Pothole size: ").append(pothole.getSize()).append("\n");
            details.append("Pothole depth: ").append(pothole.getDepth()).append("\n");
            details.append("Pothole Diameter: ").append(pothole.getDiameter()).append("\n");


            //details.append("Location: ").append(pothole.getLocation()).append(", ").append(pothole.getLocation()).append("\n");
            //details.append("Description: ").append(pothole.getDescription()).append("\n\n");
        }

        // Hiển thị thông tin chi tiết
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Pothole Details")
                .setMessage(details.toString())
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    public void showPotholeDetailsDialog2(List<Pothole> potholes) {
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
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:3000/")  // URL của server
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        // Tạo đối tượng apiService
        PotholeApiService apiService = retrofit.create(PotholeApiService.class);
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
                .baseUrl("https://nominatim.openstreetmap.org/")
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
                            //googleMap.clear();
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
//        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
//        builder.setTitle("Confirm Pothole");
//        builder.setMessage("Is this a pothole?");
//
//        builder.setPositiveButton("Pothole", (dialog, which) -> {
//            // Khi người dùng chọn Pothole, lưu lại vị trí và thông tin
//            savePothole(location);
//        });
//
//        builder.setNegativeButton("Cancel", (dialog, which) -> {
//            // Người dùng chọn Cancel, không làm gì cả
//            dialog.dismiss();
//        });
//
//        builder.show();

        //-------------------------------------------------------------------------------
        final AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
        View mView = getLayoutInflater().inflate(R.layout.confirm_pothole_dialog, null);
        alert.setView(mView);

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCancelable(false);


        mView.findViewById(R.id.cancel_cf_pothole).setOnClickListener(v -> {
            //nhan cancel khong lam gi ca
            alertDialog.dismiss();
        });

        mView.findViewById(R.id.confirm_cf_pothole).setOnClickListener(v -> {
            //save pothole
            savePothole(location);//chuc nang lac dt chua sua xong
            alertDialog.dismiss();
        });

        alertDialog.getWindow().setGravity(Gravity.BOTTOM); // de hop thoai hien ben duoi
        alertDialog.show();
        //-------------------------------------------------------------------------------
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

        LinearLayout btn_direct = dialog.findViewById(R.id.button_direct);
        LinearLayout btn_cancel = dialog.findViewById(R.id.button_cancel);
        LinearLayout btn_add_hole = dialog.findViewById(R.id.button_add_pothole);

        btn_direct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //Toast.makeText(MapFragment.this,"Edit is Clicked",Toast.LENGTH_SHORT).show();
                //getRoute(userLocation, selectedLocation);
                getDirection();
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
            Toast.makeText(requireContext(), "enable POTHOLE detect", Toast.LENGTH_SHORT).show();

        else
            Toast.makeText(requireContext(), "disable POTHOLE detect", Toast.LENGTH_SHORT).show();
    }


// chuc nang dan duong
    private void enableDirection() {
        canDirection = !canDirection;
        if(canDirection)
        {
            Toast.makeText(requireContext(), "Please, select location!", Toast.LENGTH_SHORT).show();
            getDirection();
        }
        if(!canDirection)
        {
            cancelRoute();
        }
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
        String apiKey = "5b3ce3597851110001cf624891cefefa39a74c41a58e409f95fe4da9";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openrouteservice.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OpenRouteServiceAPI api = retrofit.create(OpenRouteServiceAPI.class);
        Call<RouteResponse> call = api.getRoute(apiKey, start, end);

        call.enqueue(new Callback<RouteResponse>() {
            @Override
            public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                if (response.isSuccessful()) {
                    RouteResponse routeResponse = response.body();
                    if (routeResponse != null && routeResponse.getFeatures() != null) {
                        List<LatLng> route = new ArrayList<>();
                        for (double[] coord : routeResponse.getFeatures()[0].getGeometry().getCoordinates()) {
                            route.add(new LatLng(coord[1], coord[0]));
                        }

                        // Tự động bắt đầu dẫn đường
                        if (googleMap != null) {
                            // Vẽ Polyline lên bản đồ
                            PolylineOptions polylineOptions = new PolylineOptions()
                                    .addAll(route)
                                    .color(Color.BLUE)
                                    .width(8);
                            currentRoute = googleMap.addPolyline(polylineOptions);
                        }

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
                        if (locationResult == null || googleMap == null) return;

                        Location userLocation = locationResult.getLastLocation();
                        LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

                        // Di chuyển camera theo dõi người dùng
                        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17));
                        //googleMap.setOnMapClickListener(null);
                        // Kiểm tra nếu người dùng đến đích
                        if (isUserAtDestination(userLatLng, destination)) {
                            fusedLocationClient.removeLocationUpdates(this); // Dừng theo dõi vị trí
                            Toast.makeText(requireContext(), "You have arrived at your destination!", Toast.LENGTH_SHORT).show();
                        }
//                        if(canDirection==false)
//                        {
//                            fusedLocationClient.removeLocationUpdates(this);
//                            Toast.makeText(requireContext(), "Canceled", Toast.LENGTH_SHORT).show();
//
//                        }

                    }
                },
                Looper.getMainLooper()
        );
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
            //googleMap.setOnMapClickListener(null);
            Toast.makeText(requireContext(), "Route canceled", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "No route to cancel", Toast.LENGTH_SHORT).show();
        }
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
                        showLocationInfo(userLocation);
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
                        "username", // Thay thế bằng tên người dùng thực
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
}

