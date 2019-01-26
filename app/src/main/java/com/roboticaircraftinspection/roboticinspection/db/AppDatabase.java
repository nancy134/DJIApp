package com.roboticaircraftinspection.roboticinspection.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;

@Database(entities = {AircraftType.class, InspectionWaypoint.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AircraftTypeDao aircraftDao();
    public abstract InspectionWaypointDao inspectionWaypointDao();
/*
    static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override
        public void migrate(SupportSQLiteDatabase database){
            database.execSQL("ALTER TABLE aircrafttype ADD COLUMN noseLatitude REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE aircrafttype ADD COLUMN noseLongitude REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE aircrafttype ADD COLUMN tailLatitude REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE aircrafttype ADD COLUMN tailLongitude REAL NOT NULL DEFAULT 0.0");
        }
    };
    static final Migration MIGRATION_2_3 = new Migration(2,3){
        @Override
        public void migrate(SupportSQLiteDatabase database){
            database.execSQL("CREATE TABLE IF NOT EXISTS `inspectionwaypoint` (`id` INTEGER NOT NULL, `aircraft_id` INTEGER NOT NULL, `latitude` REAL NOT NULL, `longitude` REAL NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`aircraft_id`) REFERENCES `AircraftType`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )");
        }
    };
*/
}
