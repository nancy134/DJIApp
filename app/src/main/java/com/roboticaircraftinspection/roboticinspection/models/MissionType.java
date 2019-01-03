package com.roboticaircraftinspection.roboticinspection.models;


//<item>Select Mission Type</item>
//<item>Complete</item>
//<item>Points of Interest</item>
//<item>Camera Test</item>
//<item>Nose</item>
//<item>Tail</item>
//<item>Left Wing</item>
//<item>Right Wing</item>

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.mapbox.mapboxsdk.storage.Resource;
import com.roboticaircraftinspection.roboticinspection.InspectionApplication;
import com.roboticaircraftinspection.roboticinspection.MApplication;
import com.roboticaircraftinspection.roboticinspection.R;

public enum MissionType {
    SELECT_MISSION_TYPE(0,R.string.select_mission_type),
    TEST(1,R.string.test_application),
    COMPLETE(2,R.string.complete),
    POINTS_OF_INTEREST(3,R.string.points_of_interest),
    CAMERA_TEST(4,R.string.camera_test),
    NOSE(5,R.string.nose),
    TAIL(6,R.string.tail),
    LEFT_WING(7,R.string.left_wing),
    RIGHT_WING(8,R.string.right_wing);

    private int mResourceId;
    private int mPosition;
    MissionType(int position,int id){
        mPosition = position;
        mResourceId = id;
    }
    @Override
    public String toString(){
        Context context = MApplication.getContext();
        Resources resources = context.getResources();
        String str = resources.getString(mResourceId);
        return str;
    }
    public int id(){return mResourceId;}
    public int position(){return mPosition;}
}
