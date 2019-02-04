package com.roboticaircraftinspection.roboticinspection.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity
public class AircraftType implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name="name")
    private String name;

    @ColumnInfo(name="heading")
    private double heading;

    @ColumnInfo(name="latitude")
    private double latitude;

    @ColumnInfo(name="longitude")
    private double longitude;

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public double getHeading(){
        return heading;
    }
    public void setHeading(double heading){
        this.heading = heading;
    }
    double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude){
        this.latitude = latitude;
    }
    double getLongitude(){
        return this.longitude;
    }
    public void setLongitude(double longitude){
        this.latitude = longitude;
    }
    @Override
    public @NonNull String toString() {
        return getName();
    }
}
