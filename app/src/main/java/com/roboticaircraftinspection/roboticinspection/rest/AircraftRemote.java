package com.roboticaircraftinspection.roboticinspection.rest;

public class AircraftRemote {
    private int id;
    private String name;
    private double noseLatitude;
    private double noseLongitude;
    private double tailLatitude;
    private double tailLongitude;

    public AircraftRemote(
            int id,
            String name,
            double noseLatitude,
            double noseLongitude,
            double tailLatitude,
            double tailLongitude){
        this.id = id;
        this.name = name;
        this.noseLatitude = noseLatitude;
        this.noseLongitude = noseLongitude;
        this.tailLatitude = tailLatitude;
        this.tailLongitude = tailLongitude;
    }
    public int getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public double getNoseLatitude(){
        return this.noseLatitude;
    }

    public double getNoseLongitude() {
        return this.noseLongitude;
    }

    public double getTailLatitude() {
        return this.tailLatitude;
    }

    public double getTailLongitude() {
        return this.tailLongitude;
    }
}
