package com.roboticaircraftinspection.roboticinspection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.roboticaircraftinspection.roboticinspection.models.InitializeTest;
import com.roboticaircraftinspection.roboticinspection.models.InitializeWaypoint;

public class InitializeWaypointFragment extends Fragment implements WaypointTimeline.OnInitializeWaypointListener{

    View view;
    Button nextButton;
    Activity activity;
    WaypointTimeline waypointTimeline;
    InitializeWaypoint mInitializeWaypoint;
    int aircraftId;
    InitializeWaypointFragment.OnInitializeWaypointNextSelectedListener mCallback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_initialize_waypoint, container, false);
        activity = getActivity();
        nextButton = view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onInitializeWaypointNextSelected();
            }
        });
        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        waypointTimeline.initialize();
    }
    public void setOnInitializeWaypointNextSelectedListener(Activity activity){
        mCallback = (InitializeWaypointFragment.OnInitializeWaypointNextSelectedListener)activity;
    }

    public interface OnInitializeWaypointNextSelectedListener {
        void onInitializeWaypointNextSelected();
    }
    public void setTimeline(WaypointTimeline waypointTimeline){
        this.waypointTimeline = waypointTimeline;
        this.waypointTimeline.setOnInitializeWaypointListener(this);
    }

    public void onInitializeWaypoint(InitializeWaypoint initializeWaypoint) {
        mInitializeWaypoint = initializeWaypoint;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView aircraftFoundCheck = view.findViewById(R.id.aircraft_found_check);
                TextView aircraftFoundText = view.findViewById(R.id.aircraft_found_text);
                if (mInitializeWaypoint.aircraftFound) {
                    aircraftFoundCheck.setEnabled(true);
                    aircraftFoundText.setEnabled(true);
                } else {
                    aircraftFoundCheck.setEnabled(false);
                    aircraftFoundText.setEnabled(false);
                }
                TextView homeHeightCheck = view.findViewById(R.id.home_height_check);
                TextView homeHeightLabel = view.findViewById(R.id.home_height_label);
                TextView homeHeightValue = view.findViewById(R.id.home_height_value);
                if (mInitializeWaypoint.homeHeight != 0){
                    homeHeightCheck.setEnabled(true);
                    homeHeightLabel.setEnabled(true);
                    homeHeightValue.setText(String.valueOf(mInitializeWaypoint.homeHeight));
                } else {
                    homeHeightCheck.setEnabled(false);
                    homeHeightLabel.setEnabled(false);
                }
                TextView modelCheck = view.findViewById(R.id.model_check);
                TextView modelLabel = view.findViewById(R.id.model_label);
                TextView modelValue = view.findViewById(R.id.model_value);
                if (mInitializeWaypoint.model != null){
                    modelCheck.setEnabled(true);
                    modelLabel.setEnabled(true);
                    modelValue.setText(mInitializeWaypoint.model);
                } else {
                    modelCheck.setEnabled(false);
                    modelLabel.setEnabled(false);
                }
                TextView orientationModeCheck = view.findViewById(R.id.orientation_mode_check);
                TextView orientationModeLabel = view.findViewById(R.id.orientation_mode_label);
                TextView orientationModeValue = view.findViewById(R.id.orientation_mode_value);
                if (mInitializeWaypoint.model != null){
                    orientationModeCheck.setEnabled(true);
                    orientationModeLabel.setEnabled(true);
                    orientationModeValue.setText(mInitializeWaypoint.orientationMode);
                } else {
                    orientationModeCheck.setEnabled(false);
                    orientationModeLabel.setEnabled(false);
                }
                TextView satelliteCountCheck = view.findViewById(R.id.satellite_count_check);
                TextView satelliteCountLabel = view.findViewById(R.id.satellite_count_label);
                TextView satelliteCountValue = view.findViewById(R.id.satellite_count_value);
                if (mInitializeWaypoint.satelliteCount != 0){
                    satelliteCountCheck.setEnabled(true);
                    satelliteCountLabel.setEnabled(true);
                    satelliteCountValue.setText(String.valueOf(mInitializeWaypoint.satelliteCount));
                } else {
                    satelliteCountCheck.setEnabled(false);
                    satelliteCountLabel.setEnabled(false);
                }
                TextView flightModeCheck = view.findViewById(R.id.flight_mode_check);
                TextView flightModeLabel = view.findViewById(R.id.flight_mode_label);
                TextView flightModeValue = view.findViewById(R.id.flight_mode_value);
                if (mInitializeWaypoint.flightMode != null){
                    flightModeCheck.setEnabled(true);
                    flightModeLabel.setEnabled(true);
                    flightModeValue.setText(mInitializeWaypoint.flightMode);
                } else {
                    flightModeCheck.setEnabled(false);
                    flightModeLabel.setEnabled(false);
                }
                TextView gpsSignalLevelCheck = view.findViewById(R.id.gps_signal_level_check);
                TextView gpsSignalLevelLabel = view.findViewById(R.id.gps_signal_level_label);
                TextView gpsSignalLevelValue = view.findViewById(R.id.gps_signal_level_value);
                if (mInitializeWaypoint.gpsSignalLevel != 0){
                    gpsSignalLevelCheck.setEnabled(true);
                    gpsSignalLevelLabel.setEnabled(true);
                    gpsSignalLevelValue.setText(String.valueOf(mInitializeWaypoint.gpsSignalLevel));
                } else {
                    gpsSignalLevelCheck.setEnabled(false);
                    gpsSignalLevelLabel.setEnabled(false);
                }
                TextView serialNumberCheck = view.findViewById(R.id.serial_number_check);
                TextView serialNumberLabel = view.findViewById(R.id.serial_number_label);
                TextView serialNumberValue = view.findViewById(R.id.serial_number_value);
                if (mInitializeWaypoint.serialNumber != null){
                    serialNumberCheck.setEnabled(true);
                    serialNumberLabel.setEnabled(true);
                    serialNumberValue.setText(mInitializeWaypoint.serialNumber);
                } else {
                    serialNumberCheck.setEnabled(false);
                    serialNumberLabel.setEnabled(false);
                }
                TextView homeLatitudeCheck = view.findViewById(R.id.home_latitude_check);
                TextView homeLatitudeLabel = view.findViewById(R.id.home_latitude_label);
                TextView homeLatitudeValue = view.findViewById(R.id.home_latitude_value);
                if (mInitializeWaypoint.homeLatitude != 0){
                    homeLatitudeCheck.setEnabled(true);
                    homeLatitudeLabel.setEnabled(true);
                    homeLatitudeValue.setText(String.valueOf(mInitializeWaypoint.homeLatitude));
                } else {
                    homeLatitudeCheck.setEnabled(false);
                    homeLatitudeLabel.setEnabled(false);
                }
                TextView homeLongitudeCheck = view.findViewById(R.id.home_longitude_check);
                TextView homeLongitudeLabel = view.findViewById(R.id.home_longitude_label);
                TextView homeLongitudeValue = view.findViewById(R.id.home_longitude_value);
                if (mInitializeWaypoint.homeLongitude != 0){
                    homeLongitudeCheck.setEnabled(true);
                    homeLongitudeLabel.setEnabled(true);
                    homeLongitudeValue.setText(String.valueOf(mInitializeWaypoint.homeLongitude));
                } else {
                    homeLongitudeCheck.setEnabled(false);
                    homeLongitudeLabel.setEnabled(false);
                }
            }
        });
    }
}
