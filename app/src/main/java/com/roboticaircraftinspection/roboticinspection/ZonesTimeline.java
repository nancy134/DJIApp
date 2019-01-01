package com.roboticaircraftinspection.roboticinspection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.roboticaircraftinspection.roboticinspection.models.AcModels;
import com.roboticaircraftinspection.roboticinspection.models.HomePoint;
import com.roboticaircraftinspection.roboticinspection.models.InitializeZones;
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
import dji.common.flightcontroller.FlightOrientationMode;
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
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.timeline.TimelineMission;
import dji.sdk.mission.timeline.actions.GimbalAttitudeAction;
import dji.sdk.mission.timeline.actions.GoHomeAction;
import dji.sdk.mission.timeline.actions.TakeOffAction;
import dji.sdk.products.Aircraft;

public class ZonesTimeline extends Timeline{

    public int HOME_HEIGHT = 30;
    private String mAcModel = null;
    private boolean mFlyFromTailEnd = false;
    private String mMediaType = null;
    protected double endHomeLatitude = 0;
    protected double endHomeLongitude = 0;
    private boolean mIsGeoFenceEnabled = false;
    protected double homeLatitude = 181;
    protected double homeLongitude = 181;
    private double droneOrientation;
    ArrayList<GeoLocation> geoLocArray = new ArrayList<GeoLocation>();
    ArrayList<Double> latArray = new ArrayList<Double>();
    ArrayList<Double> longArray = new ArrayList<Double>();
    private FlightController flightController;
    private int satelliteCount;
    private int gpsSignalLevel;
    protected double oldHomeLatitude;
    protected double oldHomeLongitude;
    private MissionControl missionControl;
    private double newDroneOrientation;
    private double azimuth;
    private Waypoint fwdPointOne;
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
    private String orientationMode;
    private String serialNumber;

    private InitializeZones mInitializeZones;
    private HomePoint mHomePoint;
    ZonesTimeline.OnInitializeListener mCallback;
    ZonesTimeline.OnHomePointListener mCallbackHomePoint;

    public ZonesTimeline(
            String acModel,
            boolean isFromTailEnd,
            String mediaType,
            double endLat,
            double endLong,
            boolean isGeoFenceEnabled){
        mAcModel = acModel;
        mFlyFromTailEnd = isFromTailEnd;
        mMediaType = mediaType == null ? "BOTH" : mediaType;
        AcModels.setAcDimensions();
        endHomeLatitude = endLat;
        endHomeLongitude = endLong;
        mIsGeoFenceEnabled = isGeoFenceEnabled;
        mInitializeZones = new InitializeZones();
        mHomePoint = new HomePoint();
    }

    public void setOnInitializeListener(Fragment fragment){
        Log.d("NANCY","setOnInitializeListener");
        mCallback = (ZonesTimeline.OnInitializeListener)fragment;
    }
    public interface OnInitializeListener {
        void onInitialize(InitializeZones initializeZones);
    }
    public void setOnHomePointListener(Fragment fragment){
        mCallbackHomePoint = (ZonesTimeline.OnHomePointListener)fragment;
    }
    public interface OnHomePointListener {
        void onHomePoint(HomePoint homePoint);
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
            TimelineElement waypointFuselagePoiMissionNeFwd = TimelineMission.elementFromWaypointMission(initPoiMissionNoseEndFwd());
            elements.add(waypointFuselagePoiMissionNeFwd);
            addWaypointReachedTrigger(waypointFuselagePoiMissionNeFwd,1);
        } else {
            ToastUtils.setResultToToast("Fly from Tail end not supported");
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
                                ToastUtils.setResultToToast(" **** GEOFENCE CROSSED **** \nSTOP MISSION \nCurr Lat/long = " + currLat + " " + currLong);
                                // Abort mission
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
    private WaypointMission initPoiMissionNoseEndFwd() {
        if (!GeneralUtils.checkGpsCoordinate(homeLatitude, homeLongitude)) {
            ToastUtils.setResultToToast("No home point!!!");
            return null;
        }
        //
        //  Note:  True North is 0 degrees, positive heading is East of North, and negative heading is West of North
        //
        setTimelinePlanToText("compass angle = " + droneOrientation);
        List<Waypoint> waypoints = new LinkedList<>();
        if (endHomeLatitude != 0 && endHomeLongitude != 0) {
            setDroneOrientationUsingAzimuth();
        }
        setTimelinePlanToText(" azimuth=" + azimuth);
        WaypointMission.Builder waypointMissionBuilder = GeneralUtils.getWaypointMissionBuilder();
        //
        movePoint(homeLatitude, homeLongitude, droneOrientation, AcModels.getStartDistInMeters(mAcModel) / 4);
        Waypoint getReadyPointInitial = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel)); // Go up and forward 2.5 ft  towards A/C nose
        movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getStartDistInMeters(mAcModel) * 3 / 4);
        Waypoint getReadyPointFinal = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel)); // Go towards A/C nose 7.5 ft at the same  height
        getReadyPointFinal.addAction(new WaypointAction(WaypointActionType.ROTATE_AIRCRAFT, (int)newDroneOrientation));
        getReadyPointFinal.addAction(new WaypointAction(WaypointActionType.GIMBAL_PITCH, -90));
        movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, AcModels.getLengthInMeters(mAcModel)/5);
        fwdPointOne = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
        movePoint(newHomeLatitude, newHomeLongitude, droneOrientation, -(AcModels.getLengthInMeters(mAcModel)/5 + AcModels.getStartDistInMeters(mAcModel)));
        Waypoint homePoint = new Waypoint(newHomeLatitude, newHomeLongitude, (float) AcModels.getBodyHtInMeters(mAcModel));
        //
        //
        waypoints.add(getReadyPointInitial);
        waypoints.add(getReadyPointFinal);
        waypoints.add(fwdPointOne);
        waypoints.add(homePoint);

        //
        waypointMissionBuilder.waypointList(waypoints).waypointCount(waypoints.size());
        return waypointMissionBuilder.build();

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
    public void initialize(){
        BaseProduct product = InspectionApplication.getProductInstance();

        missionControl = MissionControl.getInstance();
        if (product instanceof Aircraft) {
            mInitializeZones.aircraftFound = true;

            flightController = ((Aircraft) product).getFlightController();

            droneOrientation = (double) flightController.getCompass().getHeading();
            mInitializeZones.droneOrientation = droneOrientation;

            setTimelinePlanToText("compass angle (init)= " + droneOrientation);

            model = product.getModel();
            mInitializeZones.model = model.getDisplayName();
            mCallback.onInitialize(mInitializeZones);

            flightController.setGoHomeHeightInMeters(HOME_HEIGHT, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null){
                        mInitializeZones.homeHeight = HOME_HEIGHT;
                        mCallback.onInitialize(mInitializeZones);
                    }
                    ToastUtils.setResultToToast("Result: " + (djiError == null
                            ? "Success"
                            : djiError.getDescription()));
                }
            });

            flightController.setFlightOrientationMode(FlightOrientationMode.COURSE_LOCK,
                    new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            ToastUtils.setResultToToast("Result: " + (djiError == null
                                    ? "Course Lock requested"
                                    : djiError.getDescription()));
                        }
                    });

            flightController.lockCourseUsingCurrentHeading(new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null){
                        mInitializeZones.lockCourseUsingCurrentHeading = true;
                        mCallback.onInitialize(mInitializeZones);
                    }
                    ToastUtils.setResultToToast("Result: " + (djiError == null
                            ? "Course Locked"
                            : djiError.getDescription()));
                }
            });

            flightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(@NonNull FlightControllerState flightControllerState) {

                    orientationMode = flightControllerState.getOrientationMode().name();
                    mInitializeZones.flightOrientationMode = orientationMode;
                    satelliteCount = flightControllerState.getSatelliteCount();
                    mInitializeZones.satelliteCount = satelliteCount;
                    flightControllerState.setFlightMode(FlightMode.GPS_HOME_LOCK);
                    mInitializeZones.flightMode = FlightMode.GPS_HOME_LOCK.name();
                    gpsSignalLevel = flightControllerState.getGPSSignalLevel().value();
                    mInitializeZones.gpsSignalLevel = gpsSignalLevel;
                    mCallback.onInitialize(mInitializeZones);
                }
            });

            flightController.getMaxFlightRadius(new CommonCallbacks.CompletionCallbackWith<Integer>() {
                @Override
                public void onSuccess(Integer s) {
                    maxFlightRadius = s;
                    mInitializeZones.maxFlightRadius = maxFlightRadius;
                    mCallback.onInitialize(mInitializeZones);
                }

                @Override
                public void onFailure(DJIError djiError) {
                    ToastUtils.setResultToToast("getMaxFlightRadius failed: " + djiError.getDescription());
                }
            });

            flightController.getSerialNumber(new CommonCallbacks.CompletionCallbackWith<String>() {
                @Override
                public void onSuccess(String s) {
                    serialNumber = s;
                    mInitializeZones.serialNumber = serialNumber;
                    mCallback.onInitialize(mInitializeZones);
                }

                @Override
                public void onFailure(DJIError djiError) {
                    ToastUtils.setResultToToast("getSerialNumber failed: " + djiError.getDescription());
                }
            });

        } else {
            mInitializeZones.aircraftFound = false;
            mCallback.onInitialize(mInitializeZones);
            ToastUtils.setResultToToast("Aircraft not found");
        }

    }
    public void getHomepoint(){
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
                                mHomePoint.latitude = homeLatitude;
                                mHomePoint.longitude = homeLongitude;
                                mCallbackHomePoint.onHomePoint(mHomePoint);
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
                        mHomePoint.latitude = homeLatitude;
                        mHomePoint.longitude = homeLongitude;
                        mCallbackHomePoint.onHomePoint(mHomePoint);
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

        } else {
            Log.d("NANCY","Not connected to aircraft");
        }
    }
}
