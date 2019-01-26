package com.roboticaircraftinspection.roboticinspection.rest;

public class AircraftRemote {
    private int id;
    private String name;
    private double heading;
    private double latitude;
    private double longitude;

    public AircraftRemote(
            int id,
            String name,
            double heading,
            double latitude,
            double longitude){
        this.id = id;
        this.name = name;
        this.heading = heading;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public int getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public double getHeading(){
        return this.heading;
    }
    public double getLatitude(){
        return this.latitude;
    }
    public double getLongitude(){
        return this.longitude;
    }

}
