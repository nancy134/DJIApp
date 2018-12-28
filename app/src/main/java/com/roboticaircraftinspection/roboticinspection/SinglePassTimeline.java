package com.roboticaircraftinspection.roboticinspection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.roboticaircraftinspection.roboticinspection.models.AcModels;
import com.roboticaircraftinspection.roboticinspection.utils.GeneralUtils;
import com.roboticaircraftinspection.roboticinspection.utils.GeoFenceUtil;
import com.roboticaircraftinspection.roboticinspection.utils.GeoLocation;
import com.roboticaircraftinspection.roboticinspection.utils.ToastUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.FlightMode;
import dji.common.flightcontroller.RTKState;
import dji.common.gimbal.Attitude;
import dji.common.gimbal.Rotation;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.model.LocationCoordinate2D;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.timeline.TimelineMission;
import dji.sdk.mission.timeline.actions.GimbalAttitudeAction;
import dji.sdk.mission.timeline.actions.GoHomeAction;
import dji.sdk.mission.timeline.actions.TakeOffAction;
import dji.sdk.products.Aircraft;

public class SinglePassTimeline extends Timeline {
    private String mAcModel = null;
    private boolean mFlyFromTailEnd = false;
    private boolean mIsGeoFenceEnabled = false;
    private String mMediaType = null;
    protected double endHomeLatitude = 0;
    protected double endHomeLongitude = 0;
    protected double homeLatitude = 181;
    protected double homeLongitude = 181;
    protected double oldHomeLatitude;
    protected double oldHomeLongitude;
    private int satelliteCount;
    private int gpsSignalLevel;
    private MissionControl missionControl;
    private double droneOrientation;
    private double newDroneOrientation;
    TimelineElement waypointFuselageMissionNe;
    private double azimuth;
    private Waypoint fwdPointFinalQuarterThree;
    private Waypoint fwdPointSecondQuarterFour;
    private Waypoint fwdPointThirdQuarterThree;
    private Waypoint fwdPointFirstQuarterThree;
    private Waypoint fwdPointFirstQuarterFour;
    private Waypoint fwdPointThirdQuarterFour;
    private Waypoint fwdPointSecondQuarterTwo;
    private Waypoint fwdPointSecondQuarterOne;
    private Waypoint fwdPointFinalQuarterFour;
    private Waypoint fwdPointSecondQuarterThree;
    private Waypoint fwdPointFinalQuarterTwo;
    private Waypoint fwdPointFirstQuarterTwo;
    private Waypoint fwdPointThirdQuarterTwo;
    private Waypoint fwdPointFirstQuarterOne;
    private Waypoint fwdPointThirdQuarterOne;
    private Waypoint fwdPointFinalQuarterOne;
    private double wingOrientation;
    ArrayList<GeoLocation> geoLocArray = new ArrayList<GeoLocation>();
    ArrayList<Double> latArray = new ArrayList<Double>();
    ArrayList<Double> longArray = new ArrayList<Double>();
    private FlightController flightController;
    protected double m210Latitude  = 0;
    protected double m210Longitude = 0;
    private Model model;
    protected double rtkbLatitude;
    protected double rtkbLongitude;
    protected double rtkmLatitude;
    protected double rtkmLongitude;
    protected double rtkfLatitude;
    protected double rtkfLongitude;
    private int maxFlightRadius;

    public SinglePassTimeline(
            String acModel,
            boolean isFromTailEnd,
            String mediaType,
            double endLat,
            double endLong,
            boolean isGeoFenceEnabled)
    {
        mAcModel = acModel;
        mFlyFromTailEnd = isFromTailEnd;
        mMediaType = mediaType == null ? "BOTH" : mediaType;
        AcModels.setAcDimensions();
        endHomeLatitude = endLat;
        endHomeLongitude = endLong;
        mIsGeoFenceEnabled = isGeoFenceEnabled;
    }
    public SinglePassTimeline(
            String acModel,
            int xVal,
            int yVal,
            int zVal,
            int oVal,
            int wpValue,
            int bhValue,
            int wAngle,
            double endLat,
            double endLong,
            boolean isGeoFenceEnabled)
    {
        mAcModel = acModel;
        AcModels.setOtherDimensions(mAcModel, xVal, yVal, zVal, oVal, wpValue, bhValue, wAngle);
        endHomeLatitude = endLat;
        endHomeLongitude = endLong;
    }
    private void initTimeline() {
        if (!GeneralUtils.checkGpsCoordinate(homeLatitude, homeLongitude)) {
            ToastUtils.setResultToToast("No home point!!!");
            return;
        } else {
            if (satelliteCount < 10) {
                ToastUtils.setResultToToast("STOP! Nbr.ofSatellites : " + satelliteCount);
                return;
            }
            if (gpsSignalLevel < 4) {
                ToastUtils.setResultToToast("STOP! Weak GPS Signal : " + gpsSignalLevel);
                return;
            }

            if (oldHomeLatitude == homeLatitude && oldHomeLongitude == homeLongitude) {
                ToastUtils.setResultToToast("old lat/long: " + oldHomeLatitude + " " + oldHomeLongitude +
                        " = " + "curr lat/long: " + homeLatitude + " " + homeLongitude);
                return;
            }
        }

        List<TimelineElement> elements = new ArrayList<>();

        missionControl = MissionControl.getInstance();
        final TimelineEvent preEvent = null;
        MissionControl.Listener listener = new MissionControl.Listener() {
            @Override
            public void onEvent(@Nullable TimelineElement element, TimelineEvent event, DJIError error) {
                updateTimelineStatus(element, event, error);
            }
        };
        newDroneOrientation = droneOrientation + 90;
        //
        //Step 1: takeoff from the ground
        setTimelinePlanToText("Step 1: takeoff from the ground");
        elements.add(new TakeOffAction());

        //Step 2: reset the gimbal to horizontal angle in 2 seconds.
        setTimelinePlanToText("Step 2: set the gimbal pitch -30 angle in 2 seconds");
        Attitude attitude = new Attitude(-30, Rotation.NO_ROTATION, Rotation.NO_ROTATION);
        GimbalAttitudeAction gimbalAction = new GimbalAttitudeAction(attitude);
        gimbalAction.setCompletionTime(2);
        elements.add(gimbalAction);

        //Step 3: start a waypoint mission while the aircraft is still recording the video
        setTimelinePlanToText("Step 3: start a waypoint mission");
        if (!mFlyFromTailEnd) {
            waypointFuselageMissionNe = TimelineMission.elementFromWaypointMission(initTestingWaypointMissionNoseEnd());
            elements.add(waypointFuselageMissionNe);
            addWaypointReachedTrigger(waypointFuselageMissionNe, 2);
        } else {
            TimelineElement waypointMissionTwo = TimelineMission.elementFromWaypointMission(initTestingWaypointMissionTailEnd());
            elements.add(waypointMissionTwo);
            addWaypointReachedTrigger(waypointMissionTwo, 3);
        }

        //Step 4: go back home
        setTimelinePlanToText("Step 4: go back home");
        elements.add(new GoHomeAction());

        //Step 5: restore gimbal attitude
        //This last action will delay the timeline to finish after land on ground, which will
        //make sure the AircraftLandedTrigger will be triggered.
        //
        setTimelinePlanToText("Step 5: set the gimbal pitch -30 angle in 2 seconds");
        attitude = new Attitude(0, Rotation.NO_ROTATION, Rotation.NO_ROTATION);
        gimbalAction = new GimbalAttitudeAction(attitude);
        gimbalAction.setCompletionTime(2);
        elements.add(gimbalAction);
        //
        // reset all Mission Control events
        //
        addAircraftLandedTrigger(missionControl);
        addBatteryPowerLevelTrigger(missionControl);

        if (missionControl.scheduledCount() > 0) {
            missionControl.unscheduleEverything();
            missionControl.removeAllListeners();
        }

        missionControl.scheduleElements(elements);
        missionControl.addListener(listener);
    }
    private void getHomepoint(){
        if (InspectionApplication.getProductInstance() instanceof Aircraft && !GeneralUtils.checkGpsCoordinate(
                homeLatitude,
                homeLongitude) && flightController != null) {
            m210Latitude = 0;
            m210Longitude = 0;
            if (model != null && ((model == Model.MATRICE_210 || model == Model.MATRICE_210_RTK))) {
                if (flightController.getRTK().isConnected()) {
                    setTimelinePlanToText("Model :" + model + " ** RTK CONNECTED **");
                    flightController.getRTK().setStateCallback(new RTKState.Callback() {
                        @Override
                        public void onUpdate(@NonNull RTKState rtkState) {
                            rtkbLatitude = rtkState.getBaseStationLocation().getLatitude(); // RTK location
                            rtkbLongitude = rtkState.getBaseStationLocation().getLongitude();
                            rtkfLatitude = rtkState.getFusionMobileStationLocation().getLatitude(); // Combination of Drone and RTK used by Flight Controller
                            rtkfLongitude = rtkState.getFusionMobileStationLocation().getLongitude();
                            rtkmLatitude = rtkState.getMobileStationLocation().getLatitude(); // Drone location, Receiver#1 Antenna
                            rtkmLongitude = rtkState.getMobileStationLocation().getLongitude();

                            // This thread continuously return back telemetry info. we dont want to set the home position all the time.
                            // To be set only once
                            if (m210Latitude == 0 && m210Longitude == 0) {
                                m210Latitude = rtkfLatitude;
                                m210Longitude = rtkfLongitude;
                                homeLatitude = m210Longitude;
                                homeLongitude = m210Longitude;
                                //
                                // The following code would help the drone return back to the new rtk home location
                                //
                                flightController.setHomeLocation(new LocationCoordinate2D(m210Latitude, m210Longitude), new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        ToastUtils.setResultToToast("setHome using RTK: " + (djiError == null
                                                ? " - Success"
                                                : " - Failed " + djiError.getDescription()));
                                        return;
                                    }
                                });
                            }

                            setTimelinePlanToText("RTKF lat/lon.: " + rtkfLatitude + " " + rtkfLongitude +
                                    "\n RTKB lat/lon.: " + rtkbLatitude + " " + rtkbLongitude +
                                    "\n RTKM lat/lon.: " + rtkmLatitude + " " + rtkmLongitude + "\n");
                            setTimelinePlanToText("New HP lat/lon: " + homeLatitude + homeLongitude + "\n");
                        }
                    });
                } else {
                    ToastUtils.setResultToToast("RTK is NOT connected");
                    return;
                }
            } else {
                flightController.getHomeLocation(new CommonCallbacks.CompletionCallbackWith<LocationCoordinate2D>() {
                    @Override
                    public void onSuccess(LocationCoordinate2D locationCoordinate2D) {
                        homeLatitude = locationCoordinate2D.getLatitude();
                        homeLongitude = locationCoordinate2D.getLongitude();
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                        ToastUtils.setResultToToast("Failed to get home coordinates: " + djiError.getDescription());
                    }
                });
            }
            setTimelinePlanToText("HP lat./Long.: " + homeLatitude + "\n" + homeLongitude +
                    "\n#satellites: " + satelliteCount + " Signal strength: " + gpsSignalLevel + "\nRadius: " + maxFlightRadius);

        }
    }
    private WaypointMission initTestingWaypointMissionNoseEnd() {
        if (!GeneralUtils.checkGpsCoordinate(homeLatitude, homeLongitude)) {
            ToastUtils.setResultToToast("No home point!!!");
            return null;
        }
        //
        //  Note:  True North is 0 degrees, positive heading is East of North, and negative heading is West of North
        //

        List<Waypoint> waypoints = new LinkedList<>();
        if (endHomeLatitude != 0 && endHomeLongitude != 0) {
            setDroneOrientationUsingAzimuth();
        }
        setTimelinePlanToText("compass angle = " + droneOrientation);

//        setTimelinePlanToText(" azimuth=" + azimuth + " " + mMediaType);

        movePoint(homeLatitude, homeLongitude, droneOrientation, AcModels.getStartDistInMeters(mAcModel) / 4);
        Waypoint getReadyPointInitial = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel)); // Go up and forward 2.5 ft  towards A/C nose
        movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getStartDistInMeters(mAcModel) * 3 / 4);
        Waypoint getReadyPointFinal = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel)); // Go towards A/C nose 7.5 ft at the same  height
        //
        if ("VIDEO".equalsIgnoreCase(mMediaType)) {

            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) * 3 / 4);
            fwdPointThirdQuarterFour = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) * 1 / 4);
            fwdPointFinalQuarterFour = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getTailHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, newDroneOrientation, AcModels.getWidthInMeters(mAcModel) / 2);
            Waypoint rightWingPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getTailHtInMeters(mAcModel));
            // Go to mid point of wing
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, ((AcModels.getLengthInMeters(mAcModel) - AcModels.getWingPosInMeters(mAcModel) - AcModels.getAngleTravelOffsetInMeters(mAcModel)) * -1));
            Waypoint wingStartPositionPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            //fly over the half wing length
            wingOrientation = droneOrientation + AcModels.getWingAngle(mAcModel);
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) * -1);
            Waypoint wingEighthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            //fly over the next half wing length
            wingOrientation = newDroneOrientation + 90 - AcModels.getWingAngle(mAcModel);
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) * -1);
            Waypoint wingFinalWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            //
            // Go to start Position
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, (AcModels.getWingPosInMeters(mAcModel) + AcModels.getStartDistInMeters(mAcModel) + AcModels.getAngleTravelOffsetInMeters(mAcModel)) * -1);
            Waypoint homeRightPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            // Move left to Home position
            movePoint(newHomeLatitude, newHomeLongitude, newDroneOrientation, AcModels.getWidthInMeters(mAcModel) / 2);
            Waypoint homePoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));

            getReadyPointFinal.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, -90));

            getReadyPointFinal.addAction(new WaypointAction(WaypointActionType.START_RECORD, 1));
            /*fwdPointFirstQuarterOne.addAction(new WaypointAction(WaypointActionType.STOP_RECORD, 1));
            wingStartPositionPoint.addAction(new WaypointAction(WaypointActionType.START_RECORD, 1));*/
            wingFinalWayPoint.addAction(new WaypointAction(WaypointActionType.STOP_RECORD, 1));
            //
            homeRightPoint.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, 0));

            waypoints.add(getReadyPointInitial);
            waypoints.add(getReadyPointFinal);
            waypoints.add(fwdPointThirdQuarterFour);
            waypoints.add(fwdPointFinalQuarterFour);
            waypoints.add(rightWingPoint);
            waypoints.add(wingStartPositionPoint);
            waypoints.add(wingEighthWayPoint);
            waypoints.add(wingFinalWayPoint);
            waypoints.add(homeRightPoint);
            waypoints.add(homePoint);


        } else {
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointFirstQuarterOne = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointFirstQuarterTwo = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointFirstQuarterThree = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointFirstQuarterFour = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));


            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointSecondQuarterOne = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointSecondQuarterTwo = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointSecondQuarterThree = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointSecondQuarterFour = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));


            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointThirdQuarterOne = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointThirdQuarterTwo = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointThirdQuarterThree = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointThirdQuarterFour = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));


            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointFinalQuarterOne = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getTailHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointFinalQuarterTwo = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getTailHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointFinalQuarterThree = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getTailHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            fwdPointFinalQuarterFour = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getTailHtInMeters(mAcModel));
//        }

            //
            // Move half the width of the aircraft
            // Wing Waypoint calculation
            //
            movePoint(newHomeLatitude, newHomeLongitude, newDroneOrientation, AcModels.getWidthInMeters(mAcModel) / 2);
            Waypoint rightWingPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getTailHtInMeters(mAcModel));
            // Go to mid point of wing
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, ((AcModels.getLengthInMeters(mAcModel) - AcModels.getWingPosInMeters(mAcModel) - AcModels.getAngleTravelOffsetInMeters(mAcModel)) * -1));
            Waypoint wingStartPositionPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            //fly over the half wing length
            wingOrientation = droneOrientation + AcModels.getWingAngle(mAcModel);
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingFirstWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingSecondWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingThirdWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingFourthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingFifthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingSixthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingSeventhWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingEighthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            //fly over the next half wing length
            wingOrientation = newDroneOrientation + 90 - AcModels.getWingAngle(mAcModel);
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingNinthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingTenthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingEleventhWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingTwelthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingThirteenthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingFourteenthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingFifteenthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingFinalWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            // Go to start Position
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, (AcModels.getWingPosInMeters(mAcModel) + AcModels.getStartDistInMeters(mAcModel) + AcModels.getAngleTravelOffsetInMeters(mAcModel)) * -1);
            Waypoint homeRightPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            // Move left to Home position
            movePoint(newHomeLatitude, newHomeLongitude, newDroneOrientation, AcModels.getWidthInMeters(mAcModel) / 2);
            Waypoint homePoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));


            //
            // Waypoint Actions for Fuselage
            //
            getReadyPointInitial.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, -45));
            getReadyPointFinal.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
            //
            getReadyPointFinal.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, -90));

            if ("BOTH".equalsIgnoreCase(mMediaType) || "VIDEO".equalsIgnoreCase(mMediaType)) {
                getReadyPointFinal.addAction(new WaypointAction(WaypointActionType.START_RECORD, 1));
                fwdPointFinalQuarterFour.addAction(new WaypointAction(WaypointActionType.STOP_RECORD, 1));
            }
            if ("BOTH".equalsIgnoreCase(mMediaType) || "PHOTO".equalsIgnoreCase(mMediaType)) {
                fwdPointFirstQuarterOne.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointFirstQuarterTwo.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointFirstQuarterThree.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointFirstQuarterFour.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointSecondQuarterOne.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointSecondQuarterTwo.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointSecondQuarterThree.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointSecondQuarterFour.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointThirdQuarterOne.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointThirdQuarterTwo.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointThirdQuarterThree.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointThirdQuarterFour.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointFinalQuarterOne.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointFinalQuarterTwo.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointFinalQuarterThree.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointFinalQuarterFour.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
            }
            //
            // Add Waypoint Actions for Wing
            //
            if ("BOTH".equalsIgnoreCase(mMediaType) || "VIDEO".equalsIgnoreCase(mMediaType)) {
                wingStartPositionPoint.addAction(new WaypointAction(WaypointActionType.START_RECORD, 1));
                wingFinalWayPoint.addAction(new WaypointAction(WaypointActionType.STOP_RECORD, 1));
            }
            if ("BOTH".equalsIgnoreCase(mMediaType) || "PHOTO".equalsIgnoreCase(mMediaType)) {

                wingFirstWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingSecondWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingThirdWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingFourthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingFifthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingSixthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingSeventhWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingEighthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingNinthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingTenthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingEleventhWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingTwelthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingThirteenthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingFourteenthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingFifteenthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingFinalWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));

            }

            homeRightPoint.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, 0));
            //
            // Add waypoints for Fuselage
            //
            waypoints.add(getReadyPointInitial);
            waypoints.add(getReadyPointFinal);
            waypoints.add(fwdPointFirstQuarterOne);
            waypoints.add(fwdPointFirstQuarterTwo);
            waypoints.add(fwdPointFirstQuarterThree);
            waypoints.add(fwdPointFirstQuarterFour);
            waypoints.add(fwdPointSecondQuarterOne);
            waypoints.add(fwdPointSecondQuarterTwo);
            waypoints.add(fwdPointSecondQuarterThree);
            waypoints.add(fwdPointSecondQuarterFour);
            waypoints.add(fwdPointThirdQuarterOne);
            waypoints.add(fwdPointThirdQuarterTwo);
            waypoints.add(fwdPointThirdQuarterThree);
            waypoints.add(fwdPointThirdQuarterFour);
            waypoints.add(fwdPointFinalQuarterOne);
            waypoints.add(fwdPointFinalQuarterTwo);
            waypoints.add(fwdPointFinalQuarterThree);
            waypoints.add(fwdPointFinalQuarterFour);
            //
            // Add waypoints for Wing
            //
            waypoints.add(rightWingPoint);
            waypoints.add(wingStartPositionPoint);
            waypoints.add(wingFirstWayPoint);
            waypoints.add(wingSecondWayPoint);
            waypoints.add(wingThirdWayPoint);
            waypoints.add(wingFourthWayPoint);
            waypoints.add(wingFifthWayPoint);
            waypoints.add(wingSixthWayPoint);
            waypoints.add(wingSeventhWayPoint);
            waypoints.add(wingEighthWayPoint);
            waypoints.add(wingNinthWayPoint);
            waypoints.add(wingTenthWayPoint);
            waypoints.add(wingEleventhWayPoint);
            waypoints.add(wingTwelthWayPoint);
            waypoints.add(wingThirteenthWayPoint);
            waypoints.add(wingFourteenthWayPoint);
            waypoints.add(wingFifteenthWayPoint);
            waypoints.add(wingFinalWayPoint);
            //
            waypoints.add(homeRightPoint);
            waypoints.add(homePoint);
        }
        //
        WaypointMission.Builder waypointMissionBuilder = GeneralUtils.getWaypointMissionBuilder();
        waypointMissionBuilder.waypointList(waypoints).waypointCount(waypoints.size());
        return waypointMissionBuilder.build();
    }
    private WaypointMission initTestingWaypointMissionTailEnd() {
        if (!GeneralUtils.checkGpsCoordinate(homeLatitude, homeLongitude)) {
            ToastUtils.setResultToToast("No home point!!!");
            return null;
        }

        List<Waypoint> waypoints = new LinkedList<>();
        if (endHomeLatitude != 0 && endHomeLongitude != 0) {
            setDroneOrientationUsingAzimuth();
        }
        movePoint(homeLatitude, homeLongitude, droneOrientation, AcModels.getStartDistInMeters(mAcModel) / 4);
        Waypoint getReadyPointInitial = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getTailHtInMeters(mAcModel)); // Go up and forward 2.5 ft  towards A/C nose
        movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getStartDistInMeters(mAcModel) * 3 / 4);
        Waypoint getReadyPointFinal = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getTailHtInMeters(mAcModel)); // Go towards A/C nose 7.5 ft at the same  height

        if ("VIDEO".equalsIgnoreCase(mMediaType)) {

            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) * 1 / 4);
            fwdPointThirdQuarterFour = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getTailHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) * 3 / 4);
            fwdPointFinalQuarterFour = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            // Go to mid point of wing
            movePoint(newHomeLatitude, newHomeLongitude, newDroneOrientation, AcModels.getWidthInMeters(mAcModel) / 2);
            Waypoint rightWingPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            //fly over the half wing length
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, ((AcModels.getWingPosInMeters(mAcModel) + AcModels.getAngleTravelOffsetInMeters(mAcModel)) * -1));
            Waypoint wingStartPositionPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));

            wingOrientation = newDroneOrientation + 90 - AcModels.getWingAngle(mAcModel);

            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) * -1);
            Waypoint wingEighthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            //fly over the next half wing length
            wingOrientation = droneOrientation + AcModels.getWingAngle(mAcModel);
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) * -1);
            Waypoint wingFinalWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            //
            // Go to start Position
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, (AcModels.getLengthInMeters(mAcModel) - AcModels.getWingPosInMeters(mAcModel) + AcModels.getStartDistInMeters(mAcModel) - AcModels.getAngleTravelOffsetInMeters(mAcModel)) * -1);
            Waypoint homeRightPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            // Move left to Home position
            movePoint(newHomeLatitude, newHomeLongitude, newDroneOrientation, AcModels.getWidthInMeters(mAcModel) / 2);
            Waypoint homePoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));

            getReadyPointFinal.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, -90));

            getReadyPointFinal.addAction(new WaypointAction(WaypointActionType.START_RECORD, 1));

            wingFinalWayPoint.addAction(new WaypointAction(WaypointActionType.STOP_RECORD, 1));
            //
            homeRightPoint.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, 0));

            waypoints.add(getReadyPointInitial);
            waypoints.add(getReadyPointFinal);
            waypoints.add(fwdPointThirdQuarterFour);
            waypoints.add(fwdPointFinalQuarterFour);
            waypoints.add(rightWingPoint);
            waypoints.add(wingStartPositionPoint);
            waypoints.add(wingEighthWayPoint);
            waypoints.add(wingFinalWayPoint);
            waypoints.add(homeRightPoint);
            waypoints.add(homePoint);


        }else {
            //
            // Go along the length of the a/c in 16 spans, for first span height = Tail height from ground level.
            // Fuselage Waypoint calculation
            //
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointFirstQuarterOne = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getTailHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointFirstQuarterTwo = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getTailHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointFirstQuarterThree = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getTailHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointFirstQuarterFour = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getTailHtInMeters(mAcModel));


            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointSecondQuarterOne = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointSecondQuarterTwo = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointSecondQuarterThree = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointSecondQuarterFour = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));


            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointThirdQuarterOne = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointThirdQuarterTwo = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointThirdQuarterThree = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointThirdQuarterFour = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));


            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointFinalQuarterOne = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointFinalQuarterTwo = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointFinalQuarterThree = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 16);
            Waypoint fwdPointFinalQuarterFour = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            //
            // Move half the width of the aircraft
            // Wing Waypoint calculation
            //
            movePoint(newHomeLatitude, newHomeLongitude, newDroneOrientation, AcModels.getWidthInMeters(mAcModel) / 2);
            Waypoint rightWingPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            // Go to mid point of wing
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, ((AcModels.getWingPosInMeters(mAcModel) + AcModels.getAngleTravelOffsetInMeters(mAcModel)) * -1));
            Waypoint wingStartPositionPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            //fly over the half wing length
            wingOrientation = newDroneOrientation + 90 - AcModels.getWingAngle(mAcModel);
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingFirstWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingSecondWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingThirdWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingFourthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingFifthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingSixthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingSeventhWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingEighthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            //fly over the next half wing length
            wingOrientation = droneOrientation + AcModels.getWingAngle(mAcModel);
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingNinthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingTenthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingEleventhWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingTwelthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingThirteenthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingFourteenthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingFifteenthWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            movePoint(newHomeLatitude, newHomeLongitude, wingOrientation, AcModels.getAngleTravelInMeters(mAcModel) / 8 * -1);
            Waypoint wingFinalWayPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            // Go to start Position
            movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, (AcModels.getLengthInMeters(mAcModel) - AcModels.getWingPosInMeters(mAcModel) + AcModels.getStartDistInMeters(mAcModel) - AcModels.getAngleTravelOffsetInMeters(mAcModel)) * -1);
            Waypoint homeRightPoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
            // Move left to Home position
            movePoint(newHomeLatitude, newHomeLongitude, newDroneOrientation, AcModels.getWidthInMeters(mAcModel) / 2);
            Waypoint homePoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));

            //
            // Add Waypoint Actions for Fuselage
            //
            getReadyPointInitial.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, -45));
            getReadyPointFinal.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
            //
            getReadyPointFinal.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, -90));
            if ("BOTH".equalsIgnoreCase(mMediaType) || "VIDEO".equalsIgnoreCase(mMediaType)) {
                getReadyPointFinal.addAction(new WaypointAction(WaypointActionType.START_RECORD, 1));
                fwdPointFinalQuarterFour.addAction(new WaypointAction(WaypointActionType.STOP_RECORD, 1));
            }
            if ("BOTH".equalsIgnoreCase(mMediaType) || "PHOTO".equalsIgnoreCase(mMediaType)) {
                fwdPointFirstQuarterOne.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointFirstQuarterTwo.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointFirstQuarterThree.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointFirstQuarterFour.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointSecondQuarterOne.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointSecondQuarterTwo.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointSecondQuarterThree.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointSecondQuarterFour.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointThirdQuarterOne.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointThirdQuarterTwo.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointThirdQuarterThree.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointThirdQuarterFour.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointFinalQuarterOne.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointFinalQuarterTwo.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointFinalQuarterThree.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                fwdPointFinalQuarterFour.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
            }
            //
            // Add Waypoint Actions for Wing
            //
            if ("BOTH".equalsIgnoreCase(mMediaType) || "VIDEO".equalsIgnoreCase(mMediaType)) {
                wingStartPositionPoint.addAction(new WaypointAction(WaypointActionType.START_RECORD, 1));
                wingFinalWayPoint.addAction(new WaypointAction(WaypointActionType.STOP_RECORD, 1));
            }
            if ("BOTH".equalsIgnoreCase(mMediaType) || "PHOTO".equalsIgnoreCase(mMediaType)) {

                wingFirstWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingSecondWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingThirdWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingFourthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingFifthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingSixthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingSeventhWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingEighthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));

                wingNinthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingTenthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingEleventhWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingTwelthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingThirteenthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingFourteenthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingFifteenthWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                wingFinalWayPoint.addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
            }
            homeRightPoint.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, 0));
            //
            // Add waypoints for Fuselage
            //
            waypoints.add(getReadyPointInitial);
            waypoints.add(getReadyPointFinal);
            waypoints.add(fwdPointFirstQuarterOne);
            waypoints.add(fwdPointFirstQuarterTwo);
            waypoints.add(fwdPointFirstQuarterThree);
            waypoints.add(fwdPointFirstQuarterFour);
            waypoints.add(fwdPointSecondQuarterOne);
            waypoints.add(fwdPointSecondQuarterTwo);
            waypoints.add(fwdPointSecondQuarterThree);
            waypoints.add(fwdPointSecondQuarterFour);
            waypoints.add(fwdPointThirdQuarterOne);
            waypoints.add(fwdPointThirdQuarterTwo);
            waypoints.add(fwdPointThirdQuarterThree);
            waypoints.add(fwdPointThirdQuarterFour);
            waypoints.add(fwdPointFinalQuarterOne);
            waypoints.add(fwdPointFinalQuarterTwo);
            waypoints.add(fwdPointFinalQuarterThree);
            waypoints.add(fwdPointFinalQuarterFour);
            //
            // Add waypoints for Wing
            //
            waypoints.add(rightWingPoint);
            waypoints.add(wingStartPositionPoint);
            waypoints.add(wingFirstWayPoint);
            waypoints.add(wingSecondWayPoint);
            waypoints.add(wingThirdWayPoint);
            waypoints.add(wingFourthWayPoint);
            waypoints.add(wingFifthWayPoint);
            waypoints.add(wingSixthWayPoint);
            waypoints.add(wingSeventhWayPoint);
            waypoints.add(wingEighthWayPoint);
            waypoints.add(wingNinthWayPoint);
            waypoints.add(wingTenthWayPoint);
            waypoints.add(wingEleventhWayPoint);
            waypoints.add(wingTwelthWayPoint);
            waypoints.add(wingThirteenthWayPoint);
            waypoints.add(wingFourteenthWayPoint);
            waypoints.add(wingFifteenthWayPoint);
            waypoints.add(wingFinalWayPoint);
            //
            waypoints.add(homeRightPoint);
            waypoints.add(homePoint);
            //
        }
        WaypointMission.Builder waypointMissionBuilder = GeneralUtils.getWaypointMissionBuilder();
        waypointMissionBuilder.waypointList(waypoints).waypointCount(waypoints.size());
        return waypointMissionBuilder.build();
    }
    private void startTimeline() {
        if (MissionControl.getInstance().scheduledCount() > 0) {
            if (mIsGeoFenceEnabled) {
                if ("MD11".equals(mAcModel)) {
                    geoLocArray = GeneralUtils.buildGeoFencePolygonTest(homeLatitude, homeLongitude, droneOrientation, mAcModel);
                }else
                {
                    geoLocArray = GeneralUtils.buildGeoFencePolygon(homeLatitude, homeLongitude, droneOrientation, mAcModel);
                }
                //
                for (int i = 0; i < geoLocArray.size(); i++) {
                    latArray.add(geoLocArray.get(i).getLatitude());
                    longArray.add(geoLocArray.get(i).getLongitude());
                }

                if (!latArray.isEmpty() && !longArray.isEmpty()) {
                    flightController.setStateCallback(new FlightControllerState.Callback() {
                        @Override
                        public void onUpdate(@NonNull FlightControllerState flightControllerState) {
                            double currLat = flightControllerState.getAircraftLocation().getLatitude();
                            double currLong = flightControllerState.getAircraftLocation().getLongitude();
                            if (!GeoFenceUtil.coordinate_is_inside_polygon(currLat, currLong, latArray, longArray)) {
                                ToastUtils.setResultToToast(" ****** GEOFENCE CROSSED ****** \n ===== MISSION HALTED =======");
                                stopTimeline();
                                flightControllerState.setFlightMode(FlightMode.ATTI_HOVER);
                            }
                        }
                    });
                }
            }
            MissionControl.getInstance().startTimeline();
        } else {
            ToastUtils.setResultToToast("Init the timeline first by clicking the Init button");
        }
    }
    private void setDroneOrientationUsingAzimuth() {
        //Note :
        //DJI Compass.getHeading() :
        // Represents the heading, in degrees. True North is 0 degrees, positive heading is East of North, and negative heading is West of North.
        // Heading bounds are [-180, 180].
        azimuth = GeneralUtils.calculateBearingUsingLatLong(homeLatitude, homeLongitude, endHomeLatitude, endHomeLongitude);
        droneOrientation = azimuth;
        newDroneOrientation = droneOrientation + 90;
        setTimelinePlanToText("azimuth=" + azimuth);
    }
}
