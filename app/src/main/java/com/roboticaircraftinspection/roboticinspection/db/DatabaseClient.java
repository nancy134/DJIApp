package com.roboticaircraftinspection.roboticinspection.db;

import android.arch.persistence.room.Room;
import android.content.Context;

import static com.roboticaircraftinspection.roboticinspection.db.AppDatabase.MIGRATION_1_2;
import static com.roboticaircraftinspection.roboticinspection.db.AppDatabase.MIGRATION_2_3;

public class DatabaseClient {
    private static DatabaseClient mInstance;

    private AppDatabase appDatabase;

    private DatabaseClient(Context mCtx){
        appDatabase = Room.databaseBuilder(mCtx, AppDatabase.class, "Inspection")
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .build();
    }
    public static synchronized DatabaseClient getInstance(Context mCtx){
        if (mInstance == null){
            mInstance = new DatabaseClient(mCtx);
        }
        return mInstance;
    }
    public AppDatabase getAppDatabase(){
        return appDatabase;
    }
}
