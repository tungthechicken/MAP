package com.example.map;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {
    private PieChart pieChartClient, pieChartServer;
    private BarChart stackBarChart;
    private String name, userCreateDate;

    private TextView totalDay, potholesUser, potholesServer;
    private RadioGroup radioGroup;
    private List<Pothole> potholes, potholeList;
    private static List<Pothole> currentMonthPotholes, currentMonthPotholeList, currentWeekPotholes, currentWeekPotholeList, alldayPotholeList, alldayPotholes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        pieChartClient = view.findViewById(R.id.pieChartClient);
        pieChartServer = view.findViewById(R.id.pieChartServer);
        stackBarChart = view.findViewById(R.id.stackBarChart);
        totalDay = view.findViewById(R.id.unitOfTime);
        potholesUser = view.findViewById(R.id.potholes);
        potholesServer = view.findViewById(R.id.potholesServer);
        radioGroup = view.findViewById(R.id.radioGroupTime);

        // Đặt giá trị mặc định cho RadioGroup
        radioGroup.check(R.id.radio_allDays);

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
        // Lấy avatar đã lưu
        SharedPreferences prefs = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        int avatarId = prefs.getInt("avatar", R.drawable.baseline_person_24); // Giá trị mặc định

        // Đặt avatar vào ImageView
        ImageView avatarImageView = view.findViewById(R.id.avatarImageView);
        avatarImageView.setImageResource(avatarId);

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
            colors.add(getResources().getColor(R.color.light_blue));
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
            colors.add(getResources().getColor(R.color.light_blue));
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
        dataSet.setColors(Color.RED, Color.rgb(255, 165, 0), Color.parseColor("#66FFFF")); // Colors for "Dangerous", "Warning", "Risky"
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
  
// Cập nhật dữ liệu khi lựa chọn thay đổi
    private void updatePotholeDataBySelection(int selectedRadioButtonId) {
        if (potholeList != null && potholes != null) {
            if (selectedRadioButtonId == R.id.radio_allDays) {  // Tất cả các ngày
                alldayPotholeList = groupPotholesByAllDay(potholeList);
                alldayPotholes = groupPotholesByAllDay(potholes);
                updateUIWithData(alldayPotholeList, alldayPotholes);
            } else if (selectedRadioButtonId == R.id.radio_week) {  // Tuần hiện tại
                currentWeekPotholeList = groupPotholesByCurrentWeek(potholeList);
                currentWeekPotholes = groupPotholesByCurrentWeek(potholes);
                updateUIWithData(currentWeekPotholeList, currentWeekPotholes);
            } else if (selectedRadioButtonId == R.id.radio_month) {  // Tháng hiện tại
                currentMonthPotholeList = groupPotholesByCurrentMonth(potholeList);
                currentMonthPotholes = groupPotholesByCurrentMonth(potholes);
                updateUIWithData(currentMonthPotholeList, currentMonthPotholes);
            }
        }
    }

    // Cập nhật UI với dữ liệu
    private void updateUIWithData(List<Pothole> userPotholes, List<Pothole> serverPotholes) {
        int totalPotholes = userPotholes.size();
        if (getActivity() != null && potholesUser != null) {
            getActivity().runOnUiThread(() -> potholesUser.setText(String.valueOf(totalPotholes)));
        }

        int totalPotholesServer = serverPotholes.size();
        if (getActivity() != null && potholesServer != null) {
            getActivity().runOnUiThread(() -> potholesServer.setText(String.valueOf(totalPotholesServer)));
        }

        // Cập nhật các biểu đồ
        setupPieChartClient(userPotholes);
        setupPieChartServer(serverPotholes);
        setupStackBarChart(serverPotholes);
    }

    //ham lay username by KIEN
    private String getUsername() {
        return name;
    }

    private void getPotholesByUsername() {
        // Lấy username
        String username = getUsername();
        // Kiểm tra username hợp lệ
        if (username == null || username.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy username", Toast.LENGTH_SHORT).show();
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
                    potholeList = response.body();
                    updatePotholeDataBySelection(R.id.radio_allDays); // Cập nhật dữ liệu mặc định
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
                    potholes = response.body();
                    updatePotholeDataBySelection(R.id.radio_allDays); // Cập nhật dữ liệu mặc định
                }
            }

            @Override
            public void onFailure(Call<List<Pothole>> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // all day
    private static List<Pothole> groupPotholesByAllDay(List<Pothole> potholes) {
        return new ArrayList<>(potholes);
    }

    // Hàm để nhóm các ổ gà theo tuần
    private static List<Pothole> groupPotholesByCurrentWeek(List<Pothole> potholes) {
        List<Pothole> currentWeek = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate currentDate = LocalDate.now();
        LocalDate mondayOfCurrentWeek = currentDate.with(DayOfWeek.MONDAY);//xác định ngày thứ 2
        long daysBetween = ChronoUnit.DAYS.between(mondayOfCurrentWeek, currentDate);
        int currentWeekOfMonth = (int) (daysBetween / 7) + 1; //tính tuần hiện tại
        // Duyệt ổ gà thuộc tuần
        for (Pothole pothole : potholes) {
            LocalDate potholeDate = LocalDate.parse(pothole.getDate().split(" ")[0], formatter);
            LocalDate potholeMonday = potholeDate.with(DayOfWeek.MONDAY);
            long potholeDaysBetween = ChronoUnit.DAYS.between(potholeMonday, currentDate);
            int potholeWeekOfMonth = (int) (potholeDaysBetween / 7) + 1;
            if (potholeWeekOfMonth == currentWeekOfMonth) {
                currentWeek.add(pothole);
            }
        }
        return currentWeek;
    }

    // Hàm lọc ổ gà theo tháng hiện tại
    private static List<Pothole> groupPotholesByCurrentMonth(List<Pothole> potholeList) {
        List<Pothole> currentMonth = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        int Month = LocalDate.now().getMonthValue();

        for (Pothole pothole : potholeList) {
            LocalDate potholeDate = LocalDate.parse(pothole.getDate().split(" ")[0], formatter);
            int potholeMonth = potholeDate.getMonthValue();
            if (potholeMonth == Month) {
                currentMonth.add(pothole);
            }
        }
        return currentMonth;
    }

    private String getRegistrationDate() {
        if (userCreateDate != null && userCreateDate.length() >= 10) {
            return userCreateDate.substring(0, 10);
        } else {
            return "Unknown Date"; // Default value if userCreateDate is invalid
        }
    }

    private void showDaysSinceRegistration() {
        String registrationDate = getRegistrationDate();
        if (!"Unknown Date".equals(registrationDate)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate regDate = LocalDate.parse(registrationDate, formatter);
            LocalDate currentDate = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(regDate, currentDate);
            totalDay.setText(String.valueOf(daysBetween));
        } else {
            totalDay.setText("N/A"); // Default value if registrationDate is invalid
        }
    }

}