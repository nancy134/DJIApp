package com.roboticaircraftinspection.roboticinspection.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(
        entity = AircraftType.class,
        parentColumns = "id",
        childColumns = "aircraft_id"),
        indices = {@Index("aircraft_id")})

public class InspectionWaypoint {
    @PrimaryKey()
    private int id;
    private int aircraft_id;

    @ColumnInfo(name="latitude")
    private double latitude;

    @ColumnInfo(name="longitude")
    private double longitude;

    @ColumnInfo(name="x")
    private double x;

    @ColumnInfo(name="y")
    private double y;

    @ColumnInfo(name="prev_waypoint")
    private int prev_waypoint;

    int getId(){
        return this.id;
    }
    void setId(int id){
        this.id = id;
    }
    int getAircraft_id(){
        return this.aircraft_id;
    }
    void setAircraft_id(int aircraftId){
        this.aircraft_id = aircraftId;
    }
    double getLatitude(){
        return this.latitude;
    }
    void setLatitude(double latitude){
        this.latitude = latitude;
    }
    double getLongitude(){
        return this.longitude;
    }
    void setLongitude(double longitude){
        this.longitude = longitude;
    }

    double getX(){
        return this.x;
    }
    void setX(double x){
        this.x = x;
    }

    double getY(){
        return this.y;
    }
    void setY(double y){
        this.y = y;
    }

    int getPrev_waypoint(){
        return this.prev_waypoint;
    }
    void setPrev_waypoint(int prev_waypoint){
        this.prev_waypoint = prev_waypoint;
    }
}
