package com.roboticaircraftinspection.roboticinspection.models;

import android.content.Context;
import android.content.res.Resources;

import com.roboticaircraftinspection.roboticinspection.MApplication;
import com.roboticaircraftinspection.roboticinspection.R;

public enum TaskType {

    SELECT_TASK(0,R.string.task_select_task),
    LOAD_WAYPOINTS(1, R.string.task_load_waypoints),
    INSPECT_AIRCRAFT(2, R.string.task_inspect_aircraft);

    private int mResourceId;
    private int mPosition;
    TaskType(int position,int id){
        mPosition = position;
        mResourceId = id;
    }
    @Override
    public String toString(){
        Context context = MApplication.getContext();
        Resources resources = context.getResources();
        return resources.getString(mResourceId);
    }
    public int id(){return mResourceId;}
    public int position(){return mPosition;}
}
