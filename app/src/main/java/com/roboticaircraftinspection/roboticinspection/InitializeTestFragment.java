package com.roboticaircraftinspection.roboticinspection;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.roboticaircraftinspection.roboticinspection.models.InitializeTest;

public class InitializeTestFragment extends Fragment implements TestTimeline.OnInitializeTestListener{

    View view;
    Button nextButton;
    Activity activity;
    InitializeTest mInitializeTest;
    TestTimeline mTestTimeline;

    InitializeTestFragment.OnInitializeTestNextSelectedListener mCallback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_initialize_test, container, false);
        activity = getActivity();
        nextButton = view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onInitializeTestNextSelected();
            }
        });
        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTestTimeline.initialize();
    }

    public void setOnInitializeTestNextSelectedListener(Activity activity){
        mCallback = (InitializeTestFragment.OnInitializeTestNextSelectedListener)activity;
    }

    public interface OnInitializeTestNextSelectedListener {
        void onInitializeTestNextSelected();
    }
    public void setTimeline(TestTimeline testTimeline){
        mTestTimeline = testTimeline;
        mTestTimeline.setOnInitializeTestListener(this);
    }
    public void onInitializeTest(InitializeTest initializeTest){
        Log.d("NANCY","InitializeTestFragment:onInitializeTest");
        mInitializeTest = initializeTest;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView aircraftFoundCheck = view.findViewById(R.id.aircraft_found_check);
                TextView aircraftFoundText = view.findViewById(R.id.aircraft_found_text);
                if (mInitializeTest.aircraftFound){
                    aircraftFoundCheck.setEnabled(true);
                    aircraftFoundText.setEnabled(true);
                } else {
                    aircraftFoundCheck.setEnabled(false);
                    aircraftFoundText.setEnabled(false);
                }
                TextView modelCheck = view.findViewById(R.id.model_check);
                TextView modelLabel = view.findViewById(R.id.model_label);
                TextView modelValue = view.findViewById(R.id.model_value);
                if (mInitializeTest.model != null){
                    modelCheck.setEnabled(true);
                    modelLabel.setEnabled(true);
                    modelValue.setText(mInitializeTest.model);
                } else {
                    modelCheck.setEnabled(false);
                    modelLabel.setEnabled(false);
                }

                TextView homeHeightCheck = view.findViewById(R.id.home_height_check);
                TextView homeHeightLabel = view.findViewById(R.id.home_height_label);
                TextView homeHeightValue = view.findViewById(R.id.home_height_value);
                if (mInitializeTest.homeHeight != 0){
                    homeHeightCheck.setEnabled(true);
                    homeHeightLabel.setEnabled(true);
                    homeHeightValue.setText(String.valueOf(mInitializeTest.homeHeight));
                } else {
                    homeHeightCheck.setEnabled(false);
                    homeHeightLabel.setEnabled(false);
                }
                TextView satelliteCountCheck = view.findViewById(R.id.satellite_count_check);
                TextView satelliteCountLabel = view.findViewById(R.id.satellite_count_label);
                TextView satelliteCountValue = view.findViewById(R.id.satellite_count_value);
                if (mInitializeTest.satelliteCount != 0){
                    satelliteCountCheck.setEnabled(true);
                    satelliteCountLabel.setEnabled(true);
                    satelliteCountValue.setText(String.valueOf(mInitializeTest.satelliteCount));
                } else {
                    satelliteCountCheck.setEnabled(false);
                    satelliteCountLabel.setEnabled(false);
                }

                TextView flightModeCheck = view.findViewById(R.id.flight_mode_check);
                TextView flightModeLabel = view.findViewById(R.id.flight_mode_label);
                TextView flightModeValue = view.findViewById(R.id.flight_mode_value);
                if (mInitializeTest.flightMode != null){
                    flightModeCheck.setEnabled(true);
                    flightModeLabel.setEnabled(true);
                    flightModeValue.setText(mInitializeTest.flightMode);
                } else {
                    flightModeCheck.setEnabled(false);
                    flightModeLabel.setEnabled(false);
                }
                //public int gpsSignalLevel = 0;
                TextView gpsSignalLevelCheck = view.findViewById(R.id.gps_signal_level_check);
                TextView gpsSignalLevelLabel = view.findViewById(R.id.gps_signal_level_label);
                TextView gpsSignalLevelValue = view.findViewById(R.id.gps_signal_level_value);
                if (mInitializeTest.gpsSignalLevel != 0){
                    gpsSignalLevelCheck.setEnabled(true);
                    gpsSignalLevelLabel.setEnabled(true);
                    gpsSignalLevelValue.setText(String.valueOf(mInitializeTest.gpsSignalLevel));
                } else {
                    gpsSignalLevelCheck.setEnabled(false);
                    gpsSignalLevelLabel.setEnabled(false);
                }
                TextView serialNumberCheck = view.findViewById(R.id.serial_number_check);
                TextView serialNumberLabel = view.findViewById(R.id.serial_number_label);
                TextView serialNumberValue = view.findViewById(R.id.serial_number_value);
                if (mInitializeTest.serialNumber != null){
                    serialNumberCheck.setEnabled(true);
                    serialNumberLabel.setEnabled(true);
                    serialNumberValue.setText(mInitializeTest.serialNumber);
                } else {
                    serialNumberCheck.setEnabled(false);
                    serialNumberLabel.setEnabled(false);
                }
            }
        });
    }
}
