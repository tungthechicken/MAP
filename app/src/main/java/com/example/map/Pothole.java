package com.example.map;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Pothole {
    private String username;
    private double size;
    private double depth;
    private double diameter;
    private String statement;
    private LocationData location;
    private String date;  // Thêm trường để lưu ngày tháng năm

    // Constructor thêm thông tin ngày tháng năm
    public Pothole(String username, double size, double depth, double diameter, String statement, LocationData location, String date) {
        this.username = username;
        this.size = size;
        this.depth = depth;
        this.diameter = diameter;
        this.statement = statement;
        this.location = location;
        this.date = getCurrentDate();
    }

    // Lớp con LocationData
    public static class LocationData {
        private String longitude;
        private String latitude;
        private String address;

        public LocationData(String longitude, String latitude, String address) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.address = address;
        }

        public String getLatitude() {
            return latitude;
        }

        public String getLongitude() {
            return longitude;
        }
    }

    // Hàm để lấy ngày tháng năm hiện tại
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  // Định dạng ngày tháng
        Date date = new Date();  // Lấy thời gian hiện tại
        return dateFormat.format(date);  // Chuyển đổi thành chuỗi
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public LocationData getLocation() {
        return location;
    }

    public void setLocation(LocationData location) {
        this.location = location;
    }

    public String getDate() {
        return date;  // Trả về thông tin ngày tháng
    }
}
