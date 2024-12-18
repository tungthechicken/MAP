package com.example.map;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {
    private PieChart pieChartClient,pieChartServer;
    private BarChart stackBarChart;
    private String name, userCreateDate;
    private TextView totalDay, potholesUser, potholesServer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        pieChartClient = view.findViewById(R.id.pieChartClient);
        pieChartServer = view.findViewById(R.id.pieChartServer);
        stackBarChart = view.findViewById(R.id.stackBarChart);
        totalDay=view.findViewById(R.id.unitOfTime);
        potholesUser=view.findViewById(R.id.potholes);
        potholesServer= view.findViewById(R.id.potholesServer);

        // Retrieve the user's name from the Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            name = bundle.getString("name");
            userCreateDate = bundle.getString("userCreatedDate");
        }
        if (name == null) {
            name = "User"; // Default value if name is null
        }

        if (userCreateDate == null) {
            userCreateDate = "Unknown Date"; // Default value if userCreateDate is null
        }

        TextView usernameTextView = view.findViewById(R.id.usernameTextView);
        usernameTextView.setText("Welcome, " + name);
        //------------------------------
        getPotholesByUsername();
        callPotholes();
        //------------------------------
        // Calculate and show days since registration
        showDaysSinceRegistration();
        return view;
    }

    private void setupPieChartClient(List<Pothole> potholeList) {
        // Khai báo các biến để lưu trữ tổng số ổ gà cho từng size
        int smallCount = 0;
        int mediumCount = 0;
        int largeCount = 0;

        // Duyệt qua danh sách potholes để đếm số lượng ổ gà cho từng kích thước
        for (Pothole pothole : potholeList) {
            int size = (int) pothole.getSize(); // Giả sử getSize() trả về giá trị float đại diện cho kích thước
            if (size == 1) {
                smallCount++;
            } else if (size == 2) {
                mediumCount++;
            } else if (size == 3) {
                largeCount++;
            }
        }

        // Tạo danh sách PieEntry từ các nhóm kích thước (size)
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        // Thêm PieEntry cho các nhóm size
        if (smallCount > 0) {
            entries.add(new PieEntry(smallCount, " " + smallCount));
            colors.add(getResources().getColor(R.color.blue));
        }
        if (mediumCount > 0) {
            entries.add(new PieEntry(mediumCount, " " + mediumCount));
            colors.add(getResources().getColor(R.color.orange));
        }
        if (largeCount > 0) {
            entries.add(new PieEntry(largeCount,  " " + largeCount));
            colors.add(getResources().getColor(R.color.red));
        }

        // Tạo PieDataSet và cài đặt màu sắc cho biểu đồ
        PieDataSet dataSet = new PieDataSet(entries, "Pothole Sizes");
        dataSet.setColors(colors);

        // Tạo PieData
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry entry) {
                return entry.getLabel();  // Hiển thị label cho mỗi phần tử
            }
        });



        pieChartClient.setData(data);
        pieChartClient.getDescription().setEnabled(false);
        pieChartClient.setDrawEntryLabels(false);
        pieChartClient.setDrawHoleEnabled(false);
        pieChartClient.getLegend().setEnabled(false);
        dataSet.setValueTextSize(18f);
        pieChartClient.invalidate();
    }
    private void setupPieChartServer(List<Pothole> potholes) {
        // Khai báo các biến để lưu trữ tổng số ổ gà cho từng size
        int smallCount = 0;
        int mediumCount = 0;
        int largeCount = 0;

        // Duyệt qua danh sách potholes để đếm số lượng ổ gà cho từng kích thước
        for (Pothole pothole : potholes) {
            int size = (int) pothole.getSize(); // Giả sử getSize() trả về giá trị float đại diện cho kích thước
            if (size == 1) {
                smallCount++;
            } else if (size == 2) {
                mediumCount++;
            } else if (size == 3) {
                largeCount++;
            }
        }

        // Tạo danh sách PieEntry từ các nhóm kích thước (size)
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        // Thêm PieEntry cho các nhóm size
        if (smallCount > 0) {
            entries.add(new PieEntry(smallCount, " " + smallCount));
            colors.add(getResources().getColor(R.color.blue));
        }
        if (mediumCount > 0) {
            entries.add(new PieEntry(mediumCount, " " + mediumCount));
            colors.add(getResources().getColor(R.color.orange));
        }
        if (largeCount > 0) {
            entries.add(new PieEntry(largeCount, " " + largeCount));
            colors.add(getResources().getColor(R.color.red));
        }

        // Tạo PieDataSet và cài đặt màu sắc cho biểu đồ
        PieDataSet dataSet = new PieDataSet(entries, "Pothole Sizes");
        dataSet.setColors(colors);

        // Tạo PieData
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry entry) {
                return entry.getLabel();  // Hiển thị label cho mỗi phần tử
            }
        });


        pieChartServer.setData(data);
        pieChartServer.getDescription().setEnabled(false);
        pieChartServer.setDrawEntryLabels(false);
        pieChartServer.setDrawHoleEnabled(false);
        pieChartServer.getLegend().setEnabled(false);
        dataSet.setValueTextSize(18f);
        pieChartServer.invalidate();
    }
    private void setupStackBarChart(List<Pothole> potholeList) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        int[] dangerousCount = new int[7];
        int[] warningCount = new int[7];
        int[] riskyCount = new int[7];

        // Categorize potholes by day of the week
        for (Pothole pothole : potholeList) {
            LocalDate date = LocalDate.parse(pothole.getDate().split(" ")[0]); // Assuming date is in "yyyy-MM-dd" format
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            int dayIndex = dayOfWeek.getValue() - 1; // Convert to 0-based index (Monday = 0, Sunday = 6)

            if (pothole.getSize() == 1) {
                riskyCount[dayIndex]++;
            } else if (pothole.getSize() == 2) {
                warningCount[dayIndex]++;
            } else if (pothole.getSize() == 3) {
                dangerousCount[dayIndex]++;
            }
        }

        // Create BarEntry objects
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, new float[]{dangerousCount[i], warningCount[i], riskyCount[i]}));
        }

        BarDataSet dataSet = new BarDataSet(entries, null);
        dataSet.setColors(Color.RED, Color.rgb(255, 165, 0), Color.BLUE); // Colors for "Dangerous", "Warning", "Risky"
        dataSet.setStackLabels(new String[]{"Dangerous", "Warning", "Risky"}); // Set labels for stack

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        stackBarChart.setData(barData);
        stackBarChart.getDescription().setEnabled(false);
        stackBarChart.invalidate();

        XAxis xAxis = stackBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}));
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawLabels(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setLabelCount(7, true);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(6);
    }

    private LineDataSet createLineDataSet(ArrayList<Entry> entries, String label, int color) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setDrawValues(true);
        return dataSet;
    }


    private float randomD() {
        return (float) (Math.random() * 10); // Trả về giá trị ngẫu nhiên từ 0 đến 10 cho "Dangerous"
    }

    private float randomW() {
        return (float) (Math.random() * 20); // Trả về giá trị ngẫu nhiên từ 0 đến 20 cho "Warning"
    }

    private float randomR() {
        return (float) (Math.random() * 30); // Trả về giá trị ngẫu nhiên từ 0 đến 30 cho "Risky"
    }
//--------------------------------------------------------------
    //ham lay username by KIEN
    private String  getUsername() {
        return name;
    }
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
                    // Tính tổng số pothole
                    int totalPotholes = potholeList.size();
                    // Update UI on the main thread
                    if (getActivity() != null && potholesUser != null) {
                        getActivity().runOnUiThread(() -> potholesUser.setText(String.valueOf(totalPotholes)));
                        setupPieChartClient(potholeList);

                        //setupLineChart(potholeList);
                    }



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
                    // Tính tổng số pothole
                    int totalPotholes = potholes.size();
                    // Update UI on the main thread
                    if (getActivity() != null && potholesServer != null) {
                        getActivity().runOnUiThread(() -> potholesServer.setText(String.valueOf(totalPotholes)));
                        setupStackBarChart(potholes);
                        setupPieChartServer(potholes);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Pothole>> call, Throwable t) {
                // Xử lý lỗi nếu có
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    //lam mau de check xem lay duoc pothole theo username de Thu lam Dashboard
//--------------------------------------------------------------
    private void showDaysSinceRegistration() {
        String registrationDate = getRegistrationDate();
        calculateDaysSinceRegistration(registrationDate);
    }
    // This method should return the registration date of the user as a String in the format "yyyy-MM-dd"
    // For example, "2023-01-01 12:34:56"
    private String getRegistrationDate() {
        // This method should return the registration date of the user as a String in the format "yyyy-MM-dd"
        // For example, "2023-01-01 12:34:56"
        String dateTime = userCreateDate; // Replace with actual logic to retrieve the region date
        return dateTime.substring(0, 10); // Return the first 10 characters (the date part)
    }
    private void calculateDaysSinceRegistration(String registrationDate) {
        // Define the date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Parse the registration date
        LocalDate regDate = LocalDate.parse(registrationDate, formatter);

        // Get the current date
        LocalDate currentDate = LocalDate.now();

        // Calculate the number of days between the registration date and the current date
        long daysBetween = ChronoUnit.DAYS.between(regDate, currentDate);

        // Display the result
        totalDay.setText(String.valueOf(daysBetween) +1);
    }
}