package com.roboticaircraftinspection.roboticinspection.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface InspectionWaypointDao {
    @Query("SELECT * FROM inspectionwaypoint")
    List<InspectionWaypoint> getAll();

    @Query("SELECT * FROM inspectionwaypoint WHERE id=:id")
    InspectionWaypoint findById(int id);

    @Query("DELETE FROM inspectionwaypoint")
    void deleteAll();

    @Insert
    void insert(InspectionWaypoint inspectionWaypoint);

    @Delete
    void delete(InspectionWaypoint inspectionWaypoint);

    @Update
    void update(InspectionWaypoint inspectionWaypoint);
}
