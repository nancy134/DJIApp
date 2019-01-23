package com.roboticaircraftinspection.roboticinspection.rest;

public class WaypointsRemote {
    private int id;
    private int aircraft_id;
    private double latitude;
    private double longitude;

    public WaypointsRemote(
            int id,
            int aircraft_id,
            double latitude,
            double longitude){
        this.id =id;
        this.aircraft_id = aircraft_id;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public int getId(){
        return this.id;
    }
    public int getAircraft_id(){
        return this.aircraft_id;
    }
    public double getLatitude(){
        return this.latitude;
    }
    public double getLongitude(){
        return this.longitude;
    }
}
