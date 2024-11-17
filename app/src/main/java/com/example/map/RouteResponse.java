package com.example.map;

public class RouteResponse {
    private Features[] features;

    public Features[] getFeatures() {
        return features;
    }

    public static class Features {
        private Geometry geometry;

        public Geometry getGeometry() {
            return geometry;
        }
    }

    public static class Geometry {
        private double[][] coordinates; // Mảng 2 chiều chứa các tọa độ

        public double[][] getCoordinates() {
            return coordinates;
        }
    }
}


