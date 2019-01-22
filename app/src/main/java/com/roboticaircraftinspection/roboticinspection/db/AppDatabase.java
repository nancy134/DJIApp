package com.roboticaircraftinspection.roboticinspection.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

@Database(entities = {AircraftType.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AircraftTypeDao aircraftDao();

    static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override
        public void migrate(SupportSQLiteDatabase database){
            database.execSQL("ALTER TABLE aircrafttype ADD COLUMN noseLatitude REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE aircrafttype ADD COLUMN noseLongitude REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE aircrafttype ADD COLUMN tailLatitude REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE aircrafttype ADD COLUMN tailLongitude REAL NOT NULL DEFAULT 0.0");
        }
    };
}
