package com.roboticaircraftinspection.roboticinspection.models;

import com.roboticaircraftinspection.roboticinspection.MApplication;
import com.roboticaircraftinspection.roboticinspection.R;

public enum StartMission {
    FROM_NOSE(R.string.from_nose),
    FROM_TAIL(R.string.from_tail);

    private int mResourceId;
    StartMission(int id){
        mResourceId = id;
    }
    @Override
    public String toString(){
        return MApplication.getContext().getString(mResourceId);
    }
    public int id(){return mResourceId;}

}
