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
import android.util.Log;

import com.roboticaircraftinspection.roboticinspection.InspectionApplication;
import com.roboticaircraftinspection.roboticinspection.R;

public enum MissionType {
    SELECT_MISSION_TYPE(R.string.select_mission_type),
    COMPLETE(R.string.complete),
    POINTS_OF_INTEREST(R.string.points_of_interest),
    CAMERA_TEST(R.string.camera_test),
    NOSE(R.string.nose),
    TAIL(R.string.tail),
    LEFT_WING(R.string.left_wing),
    RIGHT_WING(R.string.right_wing);

    private int mResourceId;
    MissionType(int id){
        mResourceId = id;
    }
    @Override
    public String toString(){
        return InspectionApplication.getContext().getString(mResourceId);
    }
    public int id(){return mResourceId;}
}
