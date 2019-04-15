package com.roboticaircraftinspection.roboticinspection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.roboticaircraftinspection.roboticinspection.db.InspectionWaypoint;
import com.roboticaircraftinspection.roboticinspection.models.InitializeWaypoint;
import com.roboticaircraftinspection.roboticinspection.utils.GeneralUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.model.LocationCoordinate2D;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.timeline.TimelineMission;
import dji.sdk.mission.timeline.actions.GoHomeAction;
import dji.sdk.mission.timeline.actions.TakeOffAction;
import dji.sdk.products.Aircraft;


public class WaypointTimeline extends Timeline {
    private InitializeWaypoint initializeWaypoint;
    private WaypointTimeline.OnInitializeWaypointListener mCallbackInitialize;
    private MissionControl missionControl;
    private int HOME_HEIGHT = 10;
    private String orientationMode;
    private int satelliteCount;
    private String flightMode;
    private int gpsSignalLevel;
    private String serialNumber;
    private List<InspectionWaypoint> inspectionWaypoints;
    private List<Waypoint> allWaypoints;
    private double aircraftHeading;
    private int currentTimeline = 0;

    WaypointTimeline(){
        initializeWaypoint = new InitializeWaypoint();
    }
    void setOnInitializeWaypointListener(Fragment fragment){
        mCallbackInitialize = (WaypointTimeline.OnInitializeWaypointListener)fragment;
    }
    public interface OnInitializeWaypointListener {
        void onInitializeWaypoint(InitializeWaypoint initializeWaypoint);
    }
    void initialize() {
        BaseProduct product = InspectionApplication.getProductInstance();

        missionControl = MissionControl.getInstance();
        if (product instanceof Aircraft) {

            Model model = product.getModel();
            initializeWaypoint.aircraftFound = true;
            initializeWaypoint.model = model.name();
            mCallbackInitialize.onInitializeWaypoint(initializeWaypoint);

            FlightController flightController = ((Aircraft) product).getFlightController();

            flightController.setGoHomeHeightInMeters(HOME_HEIGHT, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null){
                        initializeWaypoint.homeHeight = HOME_HEIGHT;
                        mCallbackInitialize.onInitializeWaypoint(initializeWaypoint);
                    }
                }
            });
            flightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(@NonNull FlightControllerState flightControllerState) {

                    orientationMode = flightControllerState.getOrientationMode().name();
                    satelliteCount = flightControllerState.getSatelliteCount();
                    flightMode = flightControllerState.getFlightMode().name();
                    gpsSignalLevel = flightControllerState.getGPSSignalLevel().value();
                    initializeWaypoint.orientationMode = orientationMode;
                    initializeWaypoint.satelliteCount = satelliteCount;
                    initializeWaypoint.flightMode = flightMode;
                    initializeWaypoint.gpsSignalLevel = gpsSignalLevel;
                    mCallbackInitialize.onInitializeWaypoint(initializeWaypoint);
                }
            });
            flightController.getSerialNumber(new CommonCallbacks.CompletionCallbackWith<String>() {
                @Override
                public void onSuccess(String s) {
                    serialNumber = s;
                    initializeWaypoint.serialNumber = serialNumber;
                    mCallbackInitialize.onInitializeWaypoint(initializeWaypoint);
                }

                @Override
                public void onFailure(DJIError djiError) {
                }
            });

            flightController.getHomeLocation(new CommonCallbacks.CompletionCallbackWith<LocationCoordinate2D>() {
                @Override
                public void onSuccess(LocationCoordinate2D locationCoordinate2D) {
                    initializeWaypoint.homeLatitude = locationCoordinate2D.getLatitude();
                    initializeWaypoint.homeLongitude = locationCoordinate2D.getLongitude();
                }

                @Override
                public void onFailure(DJIError djiError) {
                    Log.d("NANCY", "Failed to get home coordinates: " + djiError.getDescription());
                }
            });
        } else {
            mCallbackInitialize.onInitializeWaypoint(initializeWaypoint);
        }
    }
    void initTimeline(){
        allWaypoints = new LinkedList<>();

        double startLatitude, startLongitude, latitude = 0, longitude = 0, altitude;
        for (int i=0; i<inspectionWaypoints.size(); i++) {

            if (i ==0){
                startLatitude = initializeWaypoint.homeLatitude;
                startLongitude = initializeWaypoint.homeLongitude;
            } else{
                startLatitude = latitude;
                startLongitude = longitude;
            }
            double x = GeneralUtils.getX(
                    inspectionWaypoints.get(i).getX(),
                    inspectionWaypoints.get(i).getY(),
                    aircraftHeading);
            double y = GeneralUtils.getY(
                    inspectionWaypoints.get(i).getX(),
                    inspectionWaypoints.get(i).getY(),
                    aircraftHeading);
            latitude = startLatitude + x * GeneralUtils.ONE_METER_OFFSET;
            longitude = startLongitude + y * GeneralUtils.ONE_METER_OFFSET;
            altitude = (float) inspectionWaypoints.get(i).getAltitude();
            Waypoint point = new Waypoint(latitude, longitude,(float) altitude);
            allWaypoints.add(point);
        }
    }

    void startTimeline() {
        missionControl = MissionControl.getInstance();
        MissionControl.Listener listener = new MissionControl.Listener() {
            @Override
            public void onEvent(@Nullable TimelineElement element, TimelineEvent event, DJIError error) {
                updateTimelineStatus(element, event, error);
                if (event == TimelineEvent.FINISHED && element == null){

                    int MAX_WAYPOINTS = WaypointMission.MAX_WAYPOINT_COUNT;
                    //MAX_WAYPOINTS = 4;
                    int numWaypointMissions = (int)Math.floor(inspectionWaypoints.size()/MAX_WAYPOINTS) + 1;
                    ++currentTimeline;
                    if (currentTimeline < numWaypointMissions) {
                        nextTimeline(currentTimeline);
                    }
                }
            }
        };
        missionControl.addListener(listener);

        if (missionControl.scheduledCount() > 0) {
            missionControl.unscheduleEverything();
            missionControl.removeAllListeners();
        }
        nextTimeline(currentTimeline);
    }
    void nextTimeline(int i){
        int MAX_WAYPOINTS = WaypointMission.MAX_WAYPOINT_COUNT;
        //MAX_WAYPOINTS = 4;
        int numWaypointMissions = (int)Math.floor(inspectionWaypoints.size()/MAX_WAYPOINTS) + 1;

        Log.d("TIMELINE","i: "+i);

        if (missionControl.scheduledCount() > 0) {
            missionControl.unscheduleEverything();
        }
        List<TimelineElement> elements = new ArrayList<>();

        if (i == 0){
            Log.d("TIMELINE","Add Takeoff action");
            elements.add(new TakeOffAction());
        }
        WaypointMission.Builder waypointMissionBuilder = GeneralUtils.getWaypointMissionBuilder();
        List<Waypoint> waypoints;
        if (numWaypointMissions == 1){
            waypoints = allWaypoints;
        } else {
            waypoints = allWaypoints.subList(i * MAX_WAYPOINTS, (i * MAX_WAYPOINTS) + MAX_WAYPOINTS - 1);
        }
        waypointMissionBuilder.waypointList(waypoints).waypointCount(waypoints.size());
        WaypointMission waypointMission = waypointMissionBuilder.build();
        TimelineElement timelineElement = TimelineMission.elementFromWaypointMission(waypointMission);
        Log.d("TIMELINE","Add Timeline elements");
        elements.add(timelineElement);
        addWaypointReachedTrigger(timelineElement,1);

        if (i == numWaypointMissions-1){
            Log.d("TIMELINE","Add Go Home Action");
            elements.add(new GoHomeAction());
            addAircraftLandedTrigger(missionControl);
            addBatteryPowerLevelTrigger(missionControl);
        }
        missionControl.scheduleElements(elements);

        if (MissionControl.getInstance().scheduledCount() > 0) {
            Log.d("TIMELINE","StartTimeline");
            MissionControl.getInstance().startTimeline();
        }

    }
    public void setWaypoints(List<InspectionWaypoint> waypoints){
        this.inspectionWaypoints = waypoints;
    }
    public void setHeading(double heading){
        this.aircraftHeading = heading;
    }
    void logWaypoints(){
        double homeLatitude = 35.06073019;
        double homeLongitude = -89.9636959;
        double startLatitude, startLongitude, latitude = 0, longitude = 0, altitude;
        for (int i=0; i<inspectionWaypoints.size(); i++) {

            if (i ==0){
                startLatitude = homeLatitude;
                startLongitude = homeLongitude;
            } else{
                startLatitude = latitude;
                startLongitude = longitude;
            }
            double x = GeneralUtils.getX(
                    inspectionWaypoints.get(i).getX(),
                    inspectionWaypoints.get(i).getY(),
                    aircraftHeading);
            double y = GeneralUtils.getY(
                    inspectionWaypoints.get(i).getX(),
                    inspectionWaypoints.get(i).getY(),
                    aircraftHeading);
            latitude = startLatitude + x * GeneralUtils.ONE_METER_OFFSET;
            longitude = startLongitude + y * GeneralUtils.ONE_METER_OFFSET;
            altitude = (float) inspectionWaypoints.get(i).getAltitude();
            Log.d("NANCY",latitude+","+longitude+","+altitude);
        }

    }
    
}
