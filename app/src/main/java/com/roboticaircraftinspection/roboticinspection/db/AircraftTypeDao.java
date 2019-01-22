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

    @Query("DELETE FROM aircrafttype")
    void deleteAll();

    @Insert
    void insert(AircraftType aircraftType);

    @Delete
    void delete(AircraftType aircraftType);

    @Update
    void update(AircraftType aircraftType);

}
