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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {
    private PieChart pieChart;
    private LineChart lineChart;
    private BarChart stackBarChart;
    private String name;
    Button btn_debug_infor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        pieChart = view.findViewById(R.id.pieChart);
        lineChart = view.findViewById(R.id.lineChart);
        stackBarChart = view.findViewById(R.id.stackBarChart);

        // Retrieve the user's name from the Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            name = bundle.getString("name");
        }
        if (name == null) {
            name = "User"; // Default value if name is null
        }
        TextView usernameTextView = view.findViewById(R.id.usernameTextView);
        usernameTextView.setText("Welcome, " + name);

        setupPieChart();
        setupLineChart();
        setupStackBarChart();

        //------------------------------
        //DEBUG button
        btn_debug_infor = view.findViewById(R.id.btn_debug);
        btn_debug_infor.setOnClickListener(v -> getPotholesByUsername() );
        //------------------------------

        return view;
    }

    private void setupPieChart() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(30f, "Risky"));
        entries.add(new PieEntry(40f, "Warning"));
        entries.add(new PieEntry(30f, "Dangerous"));

        PieDataSet dataSet = new PieDataSet(entries, "Types");
        dataSet.setColors(getResources().getColor(R.color.yellow), getResources().getColor(R.color.orange), getResources().getColor(R.color.red));

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry entry) {
                return "";
            }
        });

        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);

        pieChart.invalidate(); // Refresh the chart
    }

    private void setupLineChart() {
        ArrayList<Entry> reportedEntries = new ArrayList<>();
        reportedEntries.add(new Entry(0, 10f));
        reportedEntries.add(new Entry(1, 20f));
        reportedEntries.add(new Entry(2, 15f));
        reportedEntries.add(new Entry(3, 25f));

        ArrayList<Entry> fixedEntries = new ArrayList<>();
        fixedEntries.add(new Entry(0, 5f));
        fixedEntries.add(new Entry(1, 10f));
        fixedEntries.add(new Entry(2, 15f));
        fixedEntries.add(new Entry(3, 20f));

        LineDataSet reportedDataSet = createLineDataSet(reportedEntries, "Reported", Color.BLUE);
        LineDataSet fixedDataSet = createLineDataSet(fixedEntries, "Fixed", Color.GREEN);

        LineData lineData = new LineData(reportedDataSet, fixedDataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();

        setupYAxisForLineChart();
    }

    private void setupStackBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();

        entries.add(new BarEntry(0, new float[]{randomD(), randomW(), randomR()})); // Monday
        entries.add(new BarEntry(1, new float[]{randomD(), randomW(), randomR()})); // Tuesday
        entries.add(new BarEntry(2, new float[]{randomD(), randomW(), randomR()})); // Wednesday
        entries.add(new BarEntry(3, new float[]{randomD(), randomW(), randomR()})); // Thursday
        entries.add(new BarEntry(4, new float[]{randomD(), randomW(), randomR()})); // Friday
        entries.add(new BarEntry(5, new float[]{randomD(), randomW(), randomR()})); // Saturday
        entries.add(new BarEntry(6, new float[]{randomD(), randomW(), randomR()})); // Sunday

        BarDataSet dataSet = new BarDataSet(entries, null);
        dataSet.setColors(Color.RED, Color.rgb(255, 165, 0), Color.YELLOW); // Colors for "Dangerous", "Warning", "Risky"
        dataSet.setStackLabels(new String[]{"Dangerous", "Warning", "Risky"}); // Set labels for stack

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        stackBarChart.setData(barData);
        stackBarChart.getDescription().setEnabled(false);
        stackBarChart.invalidate();

        XAxis xAxis = stackBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"mon", "tus", "wed", "thu", "fri", "sat", "sun"}));
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

    private void setupYAxisForLineChart() {
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setLabelCount(5, true);
        yAxis.setAxisMinimum(0);
        yAxis.setDrawLabels(true);
        yAxis.setAxisLineColor(Color.BLACK);
        yAxis.setDrawGridLines(true);

        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
                return String.valueOf((int) value);
            }
        });

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
    }
    // Các phương thức random để tạo dữ liệu ngẫu nhiên cho các loại dữ liệu trong biểu đồ cột.
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
                    showPotholeListDialog(potholeList);   //show ra cho xem trong username co cac pothole nao de tien trong viec lam dashboard
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
                    // Gọi hàm để hiển thị các pothole trên bản đồ
                    showPotholeListDialog(potholes);
                }
            }
            @Override
            public void onFailure(Call<List<Pothole>> call, Throwable t) {
                // Xử lý lỗi nếu có
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
//--------------------------------------------------------------
}