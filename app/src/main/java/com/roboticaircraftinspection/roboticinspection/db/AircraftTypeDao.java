package com.roboticaircraftinspection.roboticinspection.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;


@Dao
public interface AircraftTypeDao {
    @Query("SELECT * FROM aircrafttype")
    List<AircraftType> getAll();

    @Query("SELECT * FROM aircrafttype WHERE id=:id")
    AircraftType findById(int id);

    @Query("SELECT * FROM aircraftType WHERE name=:name")
    List<AircraftType> findByName(String name);

    @Query("DELETE FROM aircrafttype")
    void deleteAll();

    @Insert
    Long insert(AircraftType aircraftType);

    @Delete
    void delete(AircraftType aircraftType);

    @Update
    void update(AircraftType aircraftType);

}
