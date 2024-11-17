package com.example.map;
public class Pothole {
    private String username;
    private double size;
    private double depth;
    private double diameter;
    private String statement;
    private LocationData location;

    public Pothole(String username, double size, double depth, double diameter, String statement, LocationData location) {
        this.username = username;
        this.size = size;
        this.depth = depth;
        this.diameter = diameter;
        this.statement = statement;
        this.location = location;
    }

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
}

