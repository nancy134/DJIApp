package com.roboticaircraftinspection.roboticinspection.models;
public class MissionOptions {
    public Boolean photo;
    public Boolean video;
    public Boolean geofence;
    public Boolean multiplePasses;
    public StartMission startMission;
    public String aircraftType;
    public MissionType missionType;

    public MissionOptions (){
        photo = true;
        video = false;
        geofence = true;
        multiplePasses = false;
        startMission = StartMission.FROM_NOSE;
        aircraftType = AcModels.ACMODELS[0];
        missionType = MissionType.SELECT_MISSION_TYPE;
    }
    public MissionOptions(MissionOptions options){
        photo = options.photo;
        video = options.video;
        geofence = options.geofence;
        multiplePasses = options.multiplePasses;
        startMission = options.startMission;
        aircraftType = options.aircraftType;
        missionType = options.missionType;
    }
}
