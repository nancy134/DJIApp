package com.roboticaircraftinspection.roboticinspection;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

public class MApplication extends Application {

    private InspectionApplication inspectionApplication;
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);
        if (inspectionApplication == null) {
            inspectionApplication = new InspectionApplication();
            inspectionApplication.setContext(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        inspectionApplication.onCreate();
    }
}