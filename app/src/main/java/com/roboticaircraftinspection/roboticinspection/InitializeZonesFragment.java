package com.roboticaircraftinspection.roboticinspection;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.roboticaircraftinspection.roboticinspection.models.InitializeZones;
import java.math.BigDecimal;

public class InitializeZonesFragment extends Fragment implements ZonesTimeline.OnInitializeListener{
    View view;
    Button nextButton;
    InitializeZonesFragment.OnInitializeNextSelectedListener mCallback;
    ZonesTimeline mZonesTimeline;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_initialize_zones, container, false);
        nextButton = view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onInitializeNextSelected();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mZonesTimeline.initialize();
    }

    public void setTimeline(ZonesTimeline zonesTimeline){
        mZonesTimeline = zonesTimeline;
        mZonesTimeline.setOnInitializeListener(this);
    }

    public void setOnInitializeNextSelectedListener(Activity activity){
        mCallback = (InitializeZonesFragment.OnInitializeNextSelectedListener)activity;
    }

    public interface OnInitializeNextSelectedListener {
        void onInitializeNextSelected();
    }

    public void onInitialize(InitializeZones initializeZones){

        TextView aircraftFoundCheck = view.findViewById(R.id.aircraft_found_check);
        TextView aircraftFoundText = view.findViewById(R.id.aircraft_found_text);
        if (initializeZones.aircraftFound){
            aircraftFoundCheck.setEnabled(true);
            aircraftFoundText.setEnabled(true);
        } else {
            aircraftFoundCheck.setEnabled(false);
            aircraftFoundText.setEnabled(false);
        }

        TextView droneOrientationCheck = view.findViewById(R.id.drone_orientation_check);
        TextView droneOrientationLabel = view.findViewById(R.id.drone_orientation_label);
        TextView droneOrientationValue = view.findViewById(R.id.drone_orientation_value);
        droneOrientationValue.setText(String.format(new BigDecimal(initializeZones.droneOrientation).toString(), "%f"));
        if (initializeZones.droneOrientation > 0){
            droneOrientationCheck.setEnabled(true);
            droneOrientationLabel.setEnabled(true);
        } else {
            droneOrientationCheck.setEnabled(false);
            droneOrientationLabel.setEnabled(false);
        }

        TextView modelCheck = view.findViewById(R.id.model_check);
        TextView modelLabel = view.findViewById(R.id.model_label);
        TextView modelValue = view.findViewById(R.id.model_value);
        if (initializeZones.model != null){
            modelCheck.setEnabled(true);
            modelLabel.setEnabled(true);
            modelValue.setText(initializeZones.model.getDisplayName());
        } else {
            modelCheck.setEnabled(false);
            modelLabel.setEnabled(false);
        }

        TextView homeHeightCheck = view.findViewById(R.id.home_height_check);
        TextView homeHeightLabel = view.findViewById(R.id.home_height_label);
        TextView homeHeightValue = view.findViewById(R.id.home_height_value);
        if (initializeZones.homeHeight != 0){
            homeHeightCheck.setEnabled(true);
            homeHeightLabel.setEnabled(true);
            homeHeightValue.setText(initializeZones.homeHeight);
        } else {
            homeHeightCheck.setEnabled(false);
            homeHeightLabel.setEnabled(false);
        }

        TextView flightOrientationModeCheck = view.findViewById(R.id.flight_orientation_mode_check);
        TextView flightOrientationModeLabel = view.findViewById(R.id.flight_orientation_mode_label);
        TextView flightOrientationModeValue = view.findViewById(R.id.flight_orientation_mode_value);
        if (initializeZones.flightOrientationMode != null){
            flightOrientationModeCheck.setEnabled(true);
            flightOrientationModeLabel.setEnabled(true);
            flightOrientationModeValue.setText(initializeZones.flightOrientationMode);

        } else {
            flightOrientationModeCheck.setEnabled(false);
            flightOrientationModeLabel.setEnabled(false);
        }
        TextView lockCourseCheck = view.findViewById(R.id.lock_course_check);
        TextView lockCourseLabel = view.findViewById(R.id.lock_course_label);
        if (initializeZones.lockCourseUsingCurrentHeading){
            lockCourseCheck.setEnabled(true);
            lockCourseLabel.setEnabled(true);
        } else {
            lockCourseCheck.setEnabled(false);
            lockCourseLabel.setEnabled(false);
        }
        TextView satelliteCountCheck = view.findViewById(R.id.satellite_count_check);
        TextView satelliteCountLabel = view.findViewById(R.id.satellite_count_label);
        TextView satelliteCountValue = view.findViewById(R.id.satellite_count_value);
        if (initializeZones.satelliteCount != 0){
            satelliteCountCheck.setEnabled(true);
            satelliteCountLabel.setEnabled(true);
            satelliteCountValue.setText(initializeZones.satelliteCount);
        } else {
            satelliteCountCheck.setEnabled(false);
            satelliteCountLabel.setEnabled(false);
        }

        TextView flightModeCheck = view.findViewById(R.id.flight_mode_check);
        TextView flightModeLabel = view.findViewById(R.id.flight_mode_label);
        TextView flightModeValue = view.findViewById(R.id.flight_mode_value);
        if (initializeZones.flightMode != null){
            flightModeCheck.setEnabled(true);
            flightModeLabel.setEnabled(true);
            flightModeValue.setText(initializeZones.flightMode);
        } else {
            flightModeCheck.setEnabled(false);
            flightModeLabel.setEnabled(false);
        }
        //public int gpsSignalLevel = 0;
        TextView gpsSignalLevelCheck = view.findViewById(R.id.gps_signal_level_check);
        TextView gpsSignalLevelLabel = view.findViewById(R.id.gps_signal_level_label);
        TextView gpsSignalLevelValue = view.findViewById(R.id.gps_signal_level_value);
        if (initializeZones.gpsSignalLevel != 0){
            gpsSignalLevelCheck.setEnabled(true);
            gpsSignalLevelLabel.setEnabled(true);
            gpsSignalLevelValue.setText(initializeZones.gpsSignalLevel);
        } else {
            gpsSignalLevelCheck.setEnabled(false);
            gpsSignalLevelLabel.setEnabled(false);
        }
        //public int maxFlightRadius = 0;
        TextView maxFlightRadiusCheck = view.findViewById(R.id.max_flight_radius_check);
        TextView maxFlightRadiusLabel = view.findViewById(R.id.max_flight_radius_label);
        TextView maxFlightRadiusValue = view.findViewById(R.id.max_flight_radius_value);
        if (initializeZones.maxFlightRadius != 0){
            maxFlightRadiusCheck.setEnabled(true);
            maxFlightRadiusLabel.setEnabled(true);
            maxFlightRadiusValue.setText(initializeZones.maxFlightRadius);
        } else {
            maxFlightRadiusCheck.setEnabled(false);
            maxFlightRadiusLabel.setEnabled(false);
        }
        //public String serialNumber = null;
        TextView serialNumberCheck = view.findViewById(R.id.serial_number_check);
        TextView serialNumberLabel = view.findViewById(R.id.serial_number_label);
        TextView serialNumberValue = view.findViewById(R.id.serial_number_value);
        if (initializeZones.maxFlightRadius != 0){
            serialNumberCheck.setEnabled(true);
            serialNumberLabel.setEnabled(true);
            serialNumberValue.setText(initializeZones.maxFlightRadius);
        } else {
            serialNumberCheck.setEnabled(false);
            serialNumberLabel.setEnabled(false);
        }

    }
}
