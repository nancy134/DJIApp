package com.roboticaircraftinspection.roboticinspection;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.roboticaircraftinspection.roboticinspection.models.AcModels;
import com.roboticaircraftinspection.roboticinspection.utils.GeneralUtils;
import com.roboticaircraftinspection.roboticinspection.utils.ModuleVerificationUtil;
import com.roboticaircraftinspection.roboticinspection.utils.ToastUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import dji.common.camera.SettingsDefinitions;
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
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionGotoWaypointMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointTurnMode;
import dji.common.model.LocationCoordinate2D;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.Triggerable;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.timeline.TimelineMission;
import dji.sdk.mission.timeline.actions.GimbalAttitudeAction;
import dji.sdk.mission.timeline.actions.GoHomeAction;
import dji.sdk.mission.timeline.actions.TakeOffAction;
import dji.sdk.mission.timeline.triggers.AircraftLandedTrigger;
import dji.sdk.mission.timeline.triggers.BatteryPowerLevelTrigger;
import dji.sdk.mission.timeline.triggers.Trigger;
import dji.sdk.mission.timeline.triggers.TriggerEvent;
import dji.sdk.mission.timeline.triggers.WaypointReachedTrigger;
import dji.sdk.products.Aircraft;

public class CameraTestTimeline {
    private MissionControl missionControl;
    private FlightController flightController;
    private double droneOrientation;
    private String timelineInfo;
    private String runningInfo;
    private Model model;
    private String orientationMode;
    private int satelliteCount;
    private int gpsSignalLevel;
    private int maxFlightRadius;
    protected int mCameraOpticalZoom;
    private int tapZoomMultiplier = 1;
    protected double homeLatitude = 181;
    protected double homeLongitude = 181;
    protected double m210Latitude  = 0;
    protected double m210Longitude = 0;
    protected double rtkbLatitude;
    protected double rtkbLongitude;
    protected double rtkmLatitude;
    protected double rtkmLongitude;
    protected double rtkfLatitude;
    protected double rtkfLongitude;
    protected double oldHomeLatitude;
    protected double oldHomeLongitude;
    private TimelineEvent preEvent;
    private TimelineElement preElement;
    private DJIError preError;
    private double newDroneOrientation;
    private boolean mFlyFromTailEnd = false;
    private String mAcModel = null;
    protected double mCameraDist;
    private static final int earthRadiusInMetres = 6371000;
    protected double newHomeLatitude;
    protected double newHomeLongitude;
    private final int fuselageRadius= 7; // half the dia. for B757 - Body dia = 13 ft
    private int directionFactor = 1;
    private final int fuselageNbrWayPoints = 4;
    private int maxNbrPasses = 3;
    private String mMediaType = null;
    protected double mCameraDigitalZoom;

    public CameraTestTimeline(
            String acModel,
            boolean isFromTailEnd,
            String mediaType,
            double cDist,
            double cOZoom ,
            double cDZoom)
    {
        mAcModel = acModel;
        mFlyFromTailEnd = isFromTailEnd;
        mMediaType = mediaType == null ? "BOTH" : mediaType;
        mCameraDist = cDist;
        mCameraOpticalZoom = cOZoom == 0 ? 1 : (int)cOZoom;
        mCameraDigitalZoom = cDZoom == 0 ? 1 : cDZoom;
        AcModels.setAcDimensions();

    }
    public void initialize(){
        BaseProduct product = InspectionApplication.getProductInstance();
        if (product == null || !product.isConnected()) {
            ToastUtils.setResultToToast("Disconnect");
            missionControl = null;
            return;
        }
        missionControl = MissionControl.getInstance();
        if (product instanceof Aircraft) {

            flightController = ((Aircraft) product).getFlightController();
            droneOrientation = (double) flightController.getCompass().getHeading();
            setTimelinePlanToText("compass angle (init)= " + droneOrientation);
            model = product.getModel();

            flightController.setGoHomeHeightInMeters(30, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    ToastUtils.setResultToToast("Result: " + (djiError == null
                            ? "Success"
                            : djiError.getDescription()));
                }
            });
            flightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(@NonNull FlightControllerState flightControllerState) {

                    orientationMode = flightControllerState.getOrientationMode().name();
                    satelliteCount = flightControllerState.getSatelliteCount();
                    flightControllerState.setFlightMode(FlightMode.GPS_HOME_LOCK);
                    gpsSignalLevel = flightControllerState.getGPSSignalLevel().value();
                }
            });

            flightController.getMaxFlightRadius(new CommonCallbacks.CompletionCallbackWith<Integer>() {
                @Override
                public void onSuccess(Integer s) {
                    maxFlightRadius = s;
                }

                @Override
                public void onFailure(DJIError djiError) {
                    ToastUtils.setResultToToast("getMaxFlightRadius failed: " + djiError.getDescription());
                }
            });
            //  Camera Settings
            if (ModuleVerificationUtil.isCameraModuleAvailable() && InspectionApplication.getProductInstance().getCamera() != null) {
                Camera camera = InspectionApplication.getProductInstance().getCamera();
                try {
                    if (camera.isOpticalZoomSupported()) {
//                    Sets focal length of the zoom lens. It is only supported by X5, X5R and X5S camera with lens Olympus M.Zuiko ED 14-42mm f/3.5-5.6 EZ, Z3 camera and Z30 camera.
//                    Sets zoom lens focal length in units of 0.1mmcameraDJISampleApplication.getProductInstance().getCamera().isOpticalZoomSupported())
//                    Z3 - 22 to 77 mm focal length; Z30 - 5 - 129mm
                        camera.setOpticalZoomFocalLength(mCameraOpticalZoom,
                                new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        ToastUtils.setResultToToast("Result: " + (djiError == null
                                                ? "Set Optical Zoom Focal length - Success"
                                                : "Optical Zoom " + djiError.getDescription()));
                                    }
                                });
                    } else {
                        ToastUtils.setResultToToast(camera.getDisplayName() + " - Optical Zoom is not supported");
                    }
                    //
                    if (camera.isTapZoomSupported())// supported Only by Z30 Camera
                    {
                        camera.setTapZoomEnabled(true, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                ToastUtils.setResultToToast("Result: " + (djiError == null
                                        ? "Enable Tap zoom - Success"
                                        : "Enable Tap zoom " + djiError.getDescription()));
                            }
                        });
                        camera.setTapZoomMultiplier(tapZoomMultiplier, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                ToastUtils.setResultToToast("Result: " + (djiError == null
                                        ? "setTapZoomMultiplier - Success"
                                        : "setTapZoomMultiplier " + djiError.getDescription()));
                            }
                        });
                        PointF pf = new PointF(0.5f, 0.5f);
                        camera.tapZoomAtTarget(pf, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                ToastUtils.setResultToToast("Result: " + (djiError == null
                                        ? "tapZoomAtTarget - Success"
                                        : "tapZoomAtTarget " + djiError.getDescription()));
                            }
                        });

                        camera.setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO,
                                new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        ToastUtils.setResultToToast("Result: " + (djiError == null
                                                ? "camera.setMode - Success"
                                                : "camera.setMode " + djiError.getDescription()));
                                    }
                                });
                    }
                } catch (Exception e) {
                    ToastUtils.setResultToToast("Optical Zoom " + e.getMessage());
                }
            }
        }

    }
    public void getHomePoint(){
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
                        return;
                    }
                });
            }
            setTimelinePlanToText("HP lat./Long.: " + homeLatitude + "\n" + homeLongitude +
                    "\n#satellites: " + satelliteCount + " Signal strength: " + gpsSignalLevel + "\nRadius: " + maxFlightRadius);

        }
        return;

    }
    public void initTimeline(){
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
//        homeLatitude = 35.08314;
//        homeLongitude=89.67719;
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
        Attitude attitude = new Attitude(-30, Rotation.NO_ROTATION, Rotation.NO_ROTATION); // pitch,roll,yaw
        GimbalAttitudeAction gimbalAction = new GimbalAttitudeAction(attitude);
        gimbalAction.setCompletionTime(2);
        elements.add(gimbalAction);

        //Step 3: start a waypoint mission while the aircraft is still recording the video
        setTimelinePlanToText("Step 3: start a waypoint mission");
        if (!mFlyFromTailEnd) {
            if (mAcModel.equalsIgnoreCase("OTHER"))
            {
                TimelineElement waypointFuselageMissionNeOther = TimelineMission.elementFromWaypointMission(initTestingWaypointMissionNoseEndOther());
                elements.add(waypointFuselageMissionNeOther);
                addWaypointReachedTrigger(waypointFuselageMissionNeOther, 1);
            }else {
                TimelineElement waypointFuselageMissionNe = TimelineMission.elementFromWaypointMission(initTestingWaypointMissionNoseEnd());
                elements.add(waypointFuselageMissionNe);
                addWaypointReachedTrigger(waypointFuselageMissionNe, 1);
            }
        } else {
            ToastUtils.setResultToToast("Not supported : " );
            return;
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
    private void setRunningResultToText(final String s) {
        runningInfo = runningInfo + s;
    }

    private void setTimelinePlanToText(final String s) {
        timelineInfo = timelineInfo + s;
    }
    private void updateTimelineStatus(@Nullable TimelineElement element, TimelineEvent event, DJIError error) {

        if (element == preElement && event == preEvent && error == preError) {
            return;
        }

        if (element != null) {
            if (element instanceof TimelineMission) {
                setRunningResultToText(((TimelineMission) element).getMissionObject().getClass().getSimpleName()
                        + " event is "
                        + event.toString()
                        + " "
                        + (error == null ? "" : error.getDescription()));
            } else {
                setRunningResultToText(element.getClass().getSimpleName()
                        + " event is "
                        + event.toString()
                        + " "
                        + (error == null ? "" : error.getDescription()));
            }
        } else {
            setRunningResultToText("Timeline Event is " + event.toString() + " " + (error == null
                    ? ""
                    : "Failed:"
                    + error.getDescription()));
        }

        preEvent = event;
        preElement = element;
        preError = error;
    }
    private Trigger.Listener triggerListener = new Trigger.Listener() {
        @Override
        public void onEvent(Trigger trigger, TriggerEvent event, @Nullable DJIError error) {
            setRunningResultToText("Trigger " + trigger.getClass().getSimpleName() + " event is " + event.name() + (error == null ? " " : error.getDescription()));
        }
    };
    private void initTrigger(final Trigger trigger) {
        trigger.addListener(triggerListener);
        trigger.setAction(new Trigger.Action() {
            @Override
            public void onCall() {
                setRunningResultToText("Trigger " + trigger.getClass().getSimpleName() + " Action method onCall() is invoked");
            }
        });
    }
    private void addWaypointReachedTrigger(Triggerable triggerTarget, int value) {
        WaypointReachedTrigger trigger = new WaypointReachedTrigger();
        trigger.setWaypointIndex(value);
        addTrigger(trigger, triggerTarget, " at index " + value);
    }
    private void addTrigger(Trigger trigger, Triggerable triggerTarget, String additionalComment) {

        if (triggerTarget != null) {

            initTrigger(trigger);
            List<Trigger> triggers = triggerTarget.getTriggers();
            if (triggers == null) {
                triggers = new ArrayList<>();
            }

            triggers.add(trigger);
            triggerTarget.setTriggers(triggers);

            setTimelinePlanToText(triggerTarget.getClass().getSimpleName()
                    + " Trigger "
                    + triggerTarget.getTriggers().size()
                    + ") "
                    + trigger.getClass().getSimpleName()
                    + additionalComment);
        }
    }
    private WaypointMission initTestingWaypointMissionNoseEnd() {
        if (!GeneralUtils.checkGpsCoordinate(homeLatitude, homeLongitude)) {
            ToastUtils.setResultToToast("No home point!!!");
            return null;
        }
        //
        double htCorrection = (mCameraDist - 10) /3.3;
        List<Waypoint> waypoints = new LinkedList<>();
        movePoint(homeLatitude, homeLongitude, droneOrientation, AcModels.getStartDistInMeters(mAcModel) / 4);
        waypoints.add(new Waypoint(newHomeLatitude, newHomeLongitude, (float) (AcModels.getBodyHtInMeters(mAcModel) + htCorrection))); // Go up and forward 2.5 ft  towards A/C nose
        movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getStartDistInMeters(mAcModel) * 3 / 4);
        waypoints.add(new Waypoint(newHomeLatitude, newHomeLongitude, (float) (AcModels.getBodyHtInMeters(mAcModel) + htCorrection))); // Go towards A/C nose 7.5 ft at the same  height
//        waypoints.get(waypoints.size() -1 ).addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT,-90));
        //
        // Only Photos are taken in this mission, irrespective of the choice of media type
        // Mutiple pass even if you choose single pass.
        // First pass :fly along the mid point forward
        // Second Pass; Move right by d/2, come down d/4 , rotate camera by 45 degrees and return back
        // Third Pass : Move right by d/2, come down d/2, rotate camera by 90
        // Return to home point
        //
        double correctDroneOrientation = droneOrientation;
        double newBodyHeight = 0.0;
        int offsetDirectionFactor = -1;
        //
        int droneRotationAngle = (int)droneOrientation + 90;
        setTimelinePlanToText("droneRotationAngle(ini) = " + droneRotationAngle);
        if (droneRotationAngle < -180) {
            droneRotationAngle = droneRotationAngle + 180;
        }else  if (droneRotationAngle > 180) {
            droneRotationAngle = droneRotationAngle - 360;
        }
        int oldDroneRotationAngle = droneRotationAngle;
        setTimelinePlanToText("droneRotationAngle(fnl) = " + droneRotationAngle);

        //
        for (int scanNbr = 0 ; scanNbr < 2 ; scanNbr++) {
            int gimbalAngle = -90;
            newBodyHeight = AcModels.getBodyHtInMeters(mAcModel) + htCorrection;
            if (scanNbr == 1){
                maxNbrPasses = 2;
                offsetDirectionFactor = offsetDirectionFactor * - 1;
                newBodyHeight = newBodyHeight - ((fuselageRadius / 3.3) + htCorrection) * GeneralUtils.cosForDegree(45);
            }
            //
            for (int passNbr = 0; passNbr < maxNbrPasses; passNbr++) {
               /* if (passNbr > 0) {
                    newBodyHeight = newBodyHeight - ((fuselageRadius + htCorrection) /3.3) * GeneralUtils.cosForDegree(45);
                }*/
                if (scanNbr == 0) {
                    if (passNbr == 1) {
                        gimbalAngle = -45;
                    } else  if (passNbr == 2) {
                        gimbalAngle = 0;
                    }
                }else
                {
                    if (passNbr == 0) {
                        gimbalAngle = -45;
                    }else
                    {
                        gimbalAngle = 0;
                    }
                }
                //
                if (passNbr > 0)
                {
                    newBodyHeight = newBodyHeight - ((fuselageRadius /3.3) + htCorrection) * GeneralUtils.cosForDegree(45);
                    movePoint(newHomeLatitude, newHomeLongitude, correctDroneOrientation,
                            (((fuselageRadius /3.3)+ htCorrection) * GeneralUtils.sinForDegree(45)) * offsetDirectionFactor); // convert to meters )
                    waypoints.add(new Waypoint(newHomeLatitude, newHomeLongitude, (float) newBodyHeight));
                }
                //
                waypoints.get(waypoints.size() -1 ).addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, gimbalAngle));

                for (int wayPtNbr = 0; wayPtNbr < fuselageNbrWayPoints; wayPtNbr++) {
                    movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, ((AcModels.getWingPosInMeters(mAcModel) - 30 /3.3) /
                            fuselageNbrWayPoints) * directionFactor);
                    waypoints.add(new Waypoint(newHomeLatitude, newHomeLongitude, (float) newBodyHeight));
                    //
                   /* int droneRotationAngle = (int)droneOrientation + 90;
                    setTimelinePlanToText("droneRotationAngle(ini) = " + droneRotationAngle);
                    if (droneRotationAngle < -180) {
                        droneRotationAngle = droneRotationAngle + 180;
                    }else  if (droneRotationAngle > 180) {
                        droneRotationAngle = droneRotationAngle - 360;
                    }
                    setTimelinePlanToText("droneRotationAngle(aft) = " + droneRotationAngle);*/
                    if (scanNbr == 0)
                    {
                        if (passNbr > 0) {
                            if (passNbr == 1) {
                                waypoints.get(waypoints.size() - 1).turnMode = WaypointTurnMode.COUNTER_CLOCKWISE;
                            } else {
                                waypoints.get(waypoints.size() - 1).turnMode = WaypointTurnMode.CLOCKWISE;
                            }
                            waypoints.get(waypoints.size() - 1).addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, droneRotationAngle));
                        }
                    } else {
                        droneRotationAngle = oldDroneRotationAngle + 180;
                        if (droneRotationAngle < -180) {
                            droneRotationAngle = droneRotationAngle + 180;
                        }else  if (droneRotationAngle > 180) {
                            droneRotationAngle = droneRotationAngle - 360;
                        }
                        setTimelinePlanToText("scan 2# = " + oldDroneRotationAngle + "scan# " + scanNbr + " " + passNbr);
                        //
                        if (passNbr == 0) {
                            waypoints.get(waypoints.size() - 1).turnMode = WaypointTurnMode.CLOCKWISE;
                        }else
                        {
                            waypoints.get(waypoints.size() - 1).turnMode = WaypointTurnMode.COUNTER_CLOCKWISE;
                        }
                        waypoints.get(waypoints.size() - 1 ).addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT,droneRotationAngle));
                    }
                    waypoints.get(waypoints.size() - 1 ).addAction(new WaypointAction(WaypointActionType.START_TAKE_PHOTO, 1));
                }
                // Move right d/4 and down d/4 and come back with gimbal at 45 degrees - Assuming
                if (passNbr == 0) {
                    directionFactor = directionFactor * -1;
                    correctDroneOrientation = newDroneOrientation;
                } else {
                    directionFactor = directionFactor * -1;
                }
                /*if ((passNbr <= 2 && scanNbr == 0) || (passNbr <= 1 && scanNbr == 1))
                {
                    movePoint(newHomeLatitude, newHomeLongitude, correctDroneOrientation,
                            (((fuselageRadius + htCorrection) /3.3) * GeneralUtils.sinForDegree(45)) * offsetDirectionFactor); // convert to meters )
                    waypoints.add(new Waypoint(newHomeLatitude, newHomeLongitude, (float) newBodyHeight));
                }*/
            }
            // go up to fusleage height and to the left by d/2 + d/4 and perform the same sequence
            if (scanNbr < 1) {
                // Go up above fuselage before going the other side
                waypoints.add(new Waypoint(newHomeLatitude, newHomeLongitude, (float) ((AcModels.getBodyHtInMeters(mAcModel))+ htCorrection)));
//                waypoints.add(new Waypoint(newHomeLatitude, newHomeLongitude, (float)(((fuselageRadius + htCorrection) /3.3) * GeneralUtils.cosForDegree(45) * 2 * -1)));
                // Move to other side
                movePoint(newHomeLatitude, newHomeLongitude, newDroneOrientation,
                        ((fuselageRadius / 3.3 )+ htCorrection) * GeneralUtils.sinForDegree(45) * 3); // convert to meters )
                waypoints.add(new Waypoint(newHomeLatitude, newHomeLongitude, (float) ((AcModels.getBodyHtInMeters(mAcModel))+ htCorrection)));
                newBodyHeight = (AcModels.getBodyHtInMeters(mAcModel) + htCorrection
                        - (((fuselageRadius / 3.3 ) + htCorrection) * GeneralUtils.cosForDegree(45)));
                waypoints.add(new Waypoint(newHomeLatitude, newHomeLongitude, (float) newBodyHeight));
//                waypoints.get(waypoints.size() -1 ).addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT,180));
            }

        }

        // Go to start Position
        movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, (AcModels.getWingPosInMeters(mAcModel)  - 30 / 3.3 +
                AcModels.getStartDistInMeters(mAcModel) ) * -1);
        waypoints.add(new Waypoint(newHomeLatitude, newHomeLongitude, (float)newBodyHeight));
        waypoints.get(waypoints.size() -1 ).addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, 0));
        // Move left to Home position
        movePoint(newHomeLatitude, newHomeLongitude, newDroneOrientation, -((fuselageRadius / 3.3 )+ htCorrection) * GeneralUtils.sinForDegree(45) * 2);
        waypoints.add(new Waypoint(newHomeLatitude, newHomeLongitude, (float) newBodyHeight));

        //
        WaypointMission.Builder waypointMissionBuilder = GeneralUtils.getWaypointMissionBuilder();
        waypointMissionBuilder.waypointList(waypoints).waypointCount(waypoints.size());
        return waypointMissionBuilder.build();
    }
    private void movePoint(double hLat, double hLon, double brng, double distanceInMetres) {
        double brngRad = Math.toRadians(brng);
        double latRad = Math.toRadians(hLat);
        double lonRad = Math.toRadians(hLon);
        double distFrac = distanceInMetres / earthRadiusInMetres;

        double latitudeResult = Math.asin(Math.sin(latRad) * Math.cos(distFrac) + Math.cos(latRad) * Math.sin(distFrac) * Math.cos(brngRad));
        double a = Math.atan2(Math.sin(brngRad) * Math.sin(distFrac) * Math.cos(latRad), Math.cos(distFrac) - Math.sin(latRad) * Math.sin(latitudeResult));
        double longitudeResult = (lonRad + a + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        newHomeLatitude = Math.toDegrees(latitudeResult);
        newHomeLongitude = Math.toDegrees(longitudeResult);
//        System.out.println("latitude: " + Math.toDegrees(latitudeResult) + ", longitude: " + Math.toDegrees(longitudeResult));
    }
    private void addAircraftLandedTrigger(Triggerable triggerTarget) {
        AircraftLandedTrigger trigger = new AircraftLandedTrigger();
        addTrigger(trigger, triggerTarget, "");
    }
    private void addBatteryPowerLevelTrigger(Triggerable triggerTarget) {
        float value = 20f;
        BatteryPowerLevelTrigger trigger = new BatteryPowerLevelTrigger();
        trigger.setPowerPercentageTriggerValue(value);
        addTrigger(trigger, triggerTarget, " at level " + value);
    }
    private WaypointMission initTestingWaypointMissionNoseEndOther() {
        if (!GeneralUtils.checkGpsCoordinate(homeLatitude, homeLongitude)) {
            ToastUtils.setResultToToast("No home point!!!");
            return null;
        }
        //
        WaypointMission.Builder waypointMissionBuilder = new WaypointMission.Builder().autoFlightSpeed(2f)
                .maxFlightSpeed(5f)
                .setExitMissionOnRCSignalLostEnabled(false)
                .finishedAction(
                        WaypointMissionFinishedAction.NO_ACTION)
                .flightPathMode(
                        WaypointMissionFlightPathMode.NORMAL)
                .gotoFirstWaypointMode(
                        WaypointMissionGotoWaypointMode.SAFELY)
                .headingMode(
                        WaypointMissionHeadingMode.TOWARD_POINT_OF_INTEREST)
                .repeatTimes(1);
        //
        List<Waypoint> waypoints = new LinkedList<>();
        movePoint(homeLatitude, homeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 4);
        waypoints.add(new Waypoint(newHomeLatitude, newHomeLongitude, (float) (AcModels.getBodyHtInMeters(mAcModel))));
        // Go to start Position
        movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 4 * -1);
        waypoints.add(new Waypoint(newHomeLatitude, newHomeLongitude, (float) (AcModels.getBodyHtInMeters(mAcModel))));
        //
        waypoints.get(waypoints.size() -1 ).addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, -90));
        movePoint(homeLatitude, homeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 4);
        waypoints.add(new Waypoint(newHomeLatitude, newHomeLongitude, (float) (AcModels.getBodyHtInMeters(mAcModel))));
        // Go to start Position
        movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel) / 4 * -1);
        waypoints.add(new Waypoint(newHomeLatitude, newHomeLongitude, (float) (AcModels.getBodyHtInMeters(mAcModel))));
        //
        waypointMissionBuilder.waypointList(waypoints).waypointCount(waypoints.size());
        return waypointMissionBuilder.build();
    }
    private void startTimeline() {
        if (MissionControl.getInstance().scheduledCount() > 0) {
            MissionControl.getInstance().startTimeline();
        } else {
            ToastUtils.setResultToToast("Init the timeline first by clicking the Init button");
        }
    }

    private void stopTimeline() {
        MissionControl.getInstance().stopTimeline();
    }

    private void pauseTimeline() {
        MissionControl.getInstance().pauseTimeline();
    }


    private void resumeTimeline() {
        MissionControl.getInstance().resumeTimeline();
    }
}
