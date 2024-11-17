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
    }
}

