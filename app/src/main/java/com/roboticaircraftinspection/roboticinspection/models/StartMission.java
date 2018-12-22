package com.roboticaircraftinspection.roboticinspection.models;

import android.content.Context;

import com.roboticaircraftinspection.roboticinspection.InspectionApplication;
import com.roboticaircraftinspection.roboticinspection.R;

public enum StartMission {
    FROM_NOSE(R.string.from_nose),
    FROM_TAIL(R.string.from_tail);

    private int mResourceId;
    private StartMission(int id){
        mResourceId = id;
    }
    @Override
    public String toString(){
        return InspectionApplication.getContext().getString(mResourceId);
    }

}
