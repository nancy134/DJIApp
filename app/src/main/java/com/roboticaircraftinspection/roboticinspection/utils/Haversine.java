package com.roboticaircraftinspection.roboticinspection.utils;

public class Haversine {
    public static final double R = 6372.8; // In kilometers
    private double lat1;
    private double lon1;
    private double lat2;
    private double lon2;
    Haversine(double latitude1, double longitude1, double latitude2, double longitude2) {
        lat1 = latitude1;
        lon1 = longitude1;
        lat2 = latitude2;
        lon2 = longitude2;
    }
    public double getDistance() {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }
    public double getBearing() {
        double longDiff= lon2-lon1;
        double y = Math.sin(longDiff)*Math.cos(lat2);
        double x = Math.cos(lat1)*Math.sin(lat2)-Math.sin(lat1)*Math.cos(lat2)*Math.cos(longDiff);

        return Math.toDegrees((Math.atan2(y, x))+360)%360;

    }
}
