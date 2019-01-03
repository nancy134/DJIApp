package com.roboticaircraftinspection.roboticinspection;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.roboticaircraftinspection.roboticinspection.models.InitializeTest;
import com.roboticaircraftinspection.roboticinspection.models.InitializeZones;
import com.roboticaircraftinspection.roboticinspection.utils.GeneralUtils;
import com.roboticaircraftinspection.roboticinspection.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.FlightMode;
import dji.common.model.LocationCoordinate2D;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.timeline.actions.GoHomeAction;
import dji.sdk.mission.timeline.actions.TakeOffAction;
import dji.sdk.products.Aircraft;

public class TestTimeline extends Timeline {

    private MissionControl missionControl;
    private Model model;
    private int HOME_HEIGHT = 30;
    private FlightController flightController;
    private String orientationMode;
    private int satelliteCount;
    private String flightMode;
    private int gpsSignalLevel;
    private String serialNumber;
    private double homeLatitude;
    private double homeLongitude;
    private InitializeTest mInitializeTest;
    private TestTimeline.OnInitializeTestListener mCallbackInitialize;

    TestTimeline(){
        mInitializeTest = new InitializeTest();
    }

    void setOnInitializeTestListener(Fragment fragment){
        mCallbackInitialize = (TestTimeline.OnInitializeTestListener)fragment;
    }
    public interface OnInitializeTestListener {
        void onInitializeTest(InitializeTest initializeTest);
    }
    void initialize() {
        BaseProduct product = InspectionApplication.getProductInstance();

        missionControl = MissionControl.getInstance();
        if (product instanceof Aircraft) {

            model = product.getModel();
            mInitializeTest.aircraftFound = true;
            mInitializeTest.model = model.name();
            mCallbackInitialize.onInitializeTest(mInitializeTest);

            flightController = ((Aircraft) product).getFlightController();

            flightController.setGoHomeHeightInMeters(HOME_HEIGHT, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null){
                        mInitializeTest.homeHeight = HOME_HEIGHT;
                        mCallbackInitialize.onInitializeTest(mInitializeTest);
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
                    mInitializeTest.orientationMode = orientationMode;
                    mInitializeTest.satelliteCount = satelliteCount;
                    mInitializeTest.flightMode = flightMode;
                    mInitializeTest.gpsSignalLevel = gpsSignalLevel;
                    mCallbackInitialize.onInitializeTest(mInitializeTest);
                }
            });
            flightController.getSerialNumber(new CommonCallbacks.CompletionCallbackWith<String>() {
                @Override
                public void onSuccess(String s) {
                    serialNumber = s;
                    mInitializeTest.serialNumber = serialNumber;
                    mCallbackInitialize.onInitializeTest(mInitializeTest);
                }

                @Override
                public void onFailure(DJIError djiError) {
                }
            });

        } else {
            mCallbackInitialize.onInitializeTest(mInitializeTest);
        }
    }
    public void getEndPoint(){
        getHomepoint();
        mInitializeTest.endLatitude = homeLatitude;
        mInitializeTest.endLongitude = homeLongitude;
        mCallbackInitialize.onInitializeTest(mInitializeTest);
    }
    public void getStartingPoint(){
        getHomepoint();
        mInitializeTest.startLatitude = homeLatitude;
        mInitializeTest.startLongitude = homeLongitude;
        mCallbackInitialize.onInitializeTest(mInitializeTest);
    }
    void getHomepoint(){
        if (InspectionApplication.getProductInstance() instanceof Aircraft &&
                !GeneralUtils.checkGpsCoordinate(homeLatitude, homeLongitude) &&
                flightController != null) {
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
    }
    void initTimeline(){
        missionControl = MissionControl.getInstance();
        MissionControl.Listener listener = new MissionControl.Listener() {
            @Override
            public void onEvent(@Nullable TimelineElement element, TimelineEvent event, DJIError error) {
                updateTimelineStatus(element, event, error);
            }
        };
        List<TimelineElement> elements = new ArrayList<>();
        elements.add(new TakeOffAction());
        elements.add(new GoHomeAction());
        addAircraftLandedTrigger(missionControl);

        if (missionControl.scheduledCount() > 0) {
            missionControl.unscheduleEverything();
            missionControl.removeAllListeners();
        }

        missionControl.scheduleElements(elements);
        missionControl.addListener(listener);
    }
    public void startTimeline() {
        if (MissionControl.getInstance().scheduledCount() > 0) {
            MissionControl.getInstance().startTimeline();
        }
    }
}
