package com.roboticaircraftinspection.roboticinspection.utils;

public class GeoLocation {

    double lat;
    double lng;

    public GeoLocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLatitude()
    {
        return lat;
    }

    public double getLongitude()
    {
        return lng;
    }

    @Override
    public String toString() {
        return "(" + lat + "," + lng + ")";
    }

}
