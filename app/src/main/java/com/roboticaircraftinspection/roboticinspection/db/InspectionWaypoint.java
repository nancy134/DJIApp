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
}
