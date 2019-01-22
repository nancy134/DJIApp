package com.roboticaircraftinspection.roboticinspection.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class AircraftType implements Serializable {
    @PrimaryKey()
    private int id;

    @ColumnInfo(name="name")
    private String name;

    @ColumnInfo(name="noseLatitude")
    private double noseLatitude = 0.0;

    @ColumnInfo(name="noseLongitude")
    private double noseLongitude = 0.0;

    @ColumnInfo(name="tailLatitude")
    private double tailLatitude = 0.0;

    @ColumnInfo(name="tailLongitude")
    private double tailLongitude = 0.0;

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

    double getNoseLatitude(){
        return noseLatitude;
    }
    public void setNoseLatitude(double noseLatitude){
        this.noseLatitude = noseLatitude;
    }
    double getNoseLongitude(){
        return noseLongitude;
    }
    public void setNoseLongitude(double noseLongitude){
        this.noseLongitude = noseLongitude;
    }
    double getTailLatitude(){
        return tailLatitude;
    }
    public void setTailLatitude(double tailLatitude){
        this.tailLatitude = tailLatitude;
    }
    double getTailLongitude() {
        return tailLongitude;
    }
    public void setTailLongitude(double tailLongitude){
        this.tailLongitude = tailLongitude;
    }
}
