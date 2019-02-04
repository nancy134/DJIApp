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
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int aircraft_id;

    @ColumnInfo(name="x")
    private double x;

    @ColumnInfo(name="y")
    private double y;

    @ColumnInfo(name="altitude")
    private double altitude;

    @ColumnInfo(name="heading")
    private double heading;

    int getId(){
        return this.id;
    }
    public void setId(int id){
        this.id = id;
    }

    int getAircraft_id(){
        return this.aircraft_id;
    }
    public void setAircraft_id(int aircraftId){
        this.aircraft_id = aircraftId;
    }

    public double getX(){
        return this.x;
    }
    public void setX(double x){
        this.x = x;
    }

    public double getY(){
        return this.y;
    }
    public void setY(double y){
        this.y = y;
    }

    public double getAltitude(){
        return this.altitude;
    }
    public void setAltitude(double altitude){
        this.altitude = altitude;
    }

    double getHeading(){
        return this.heading;
    }
    public void setHeading(double heading){
        this.heading = heading;
    }

}
