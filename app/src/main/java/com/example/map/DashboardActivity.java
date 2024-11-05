package com.example.map;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {
    private TextView km, potholes, unitOfTime;
    private Spinner labelSpinner;
    private PieChart pieChart;
    private LineChart lineChart;
    private BarChart stackBarChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Setup navigation bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                return true;
            }
            if (item.getItemId() == R.id.nav_map) {
                startActivity(new Intent(getApplicationContext(), MapActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            if (item.getItemId() == R.id.nav_settings) {
                startActivity(new Intent(getApplicationContext(), SettingActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        km = findViewById(R.id.km);
        potholes = findViewById(R.id.potholes);
        unitOfTime = findViewById(R.id.unitOfTime);
        labelSpinner = findViewById(R.id.labelSpinner);
        pieChart = findViewById(R.id.pieChart);
        lineChart = findViewById(R.id.lineChart);
        stackBarChart = findViewById(R.id.stackBarChart);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.date_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        labelSpinner.setAdapter(adapter);
        labelSpinner.setSelection(adapter.getPosition("Day"));

        updateValues("Day");

        km.setText(String.valueOf(getInitialKilometers()));
        potholes.setText(String.valueOf(getInitialPotholes()));

        setupPieChart();
        setupLineChart();
        setupStackBarChart();


        // Set up the settings button
        ImageView settingButton = findViewById(R.id.settingButton);
        settingButton.setOnClickListener(view -> {
            // Handle settings button click
            Intent intent = new Intent(DashboardActivity.this, SettingActivity.class);
            startActivity(intent);
        });

        // Set up the map button
        ImageView mapButton = findViewById(R.id.mapButton);
        mapButton.setOnClickListener(view -> {
            // Handle map button click
            Intent intent = new Intent(DashboardActivity.this, MapActivity.class);
            startActivity(intent);
        });


        labelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();
                updateValues(selectedOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì
            }
        });

        unitOfTime.setText(String.valueOf(getInitialTimeUnits()));
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
                return ""; // Không hiển thị giá trị
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

        entries.add(new BarEntry(0, new float[]{randomD(), randomW(), randomR()})); // Thứ 2
        entries.add(new BarEntry(1, new float[]{randomD(), randomW(), randomR()})); // Thứ 3
        entries.add(new BarEntry(2, new float[]{randomD(), randomW(), randomR()})); // Thứ 4
        entries.add(new BarEntry(3, new float[]{randomD(), randomW(), randomR()})); // Thứ 5
        entries.add(new BarEntry(4, new float[]{randomD(), randomW(), randomR()})); // Thứ 6
        entries.add(new BarEntry(5, new float[]{randomD(), randomW(), randomR()})); // Thứ 7
        entries.add(new BarEntry(6, new float[]{randomD(), randomW(), randomR()})); // Chủ nhật

        BarDataSet dataSet = new BarDataSet(entries, null);
        dataSet.setColors(Color.RED, Color.rgb(255, 165, 0), Color.YELLOW); // Màu cho "Dangerous", "Warning", "Risky"
        dataSet.setStackLabels(new String[]{"Dangerous", "Warning", "Risky"}); // Thiết lập nhãn cho stack

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

    private void updateValues(String option) {
        switch (option) {
            case "Day":
                unitOfTime.setText(String.valueOf(getQuantityForDays()));
                break;
            case "Week":
                unitOfTime.setText(String.valueOf(getQuantityForWeeks()));
                break;
            case "Month":
                unitOfTime.setText(String.valueOf(getQuantityForMonths()));
                break;
        }
    }

    private int getInitialKilometers() {
        return 0;
    }

    private int getInitialPotholes() {
        return 50;
    }

    private int getInitialTimeUnits() {
        return 0;
    }

    private int getQuantityForDays() {
        return 14;
    }

    private int getQuantityForWeeks() {
        return 5;
    }

    private int getQuantityForMonths() {
        return 2;
    }

    private float randomD() {
        return (float) (Math.random() * 100);
    }

    private float randomW() {
        return (float) (Math.random() * 100);
    }

    private float randomR() {
        return (float) (Math.random() * 100);
    }
}
