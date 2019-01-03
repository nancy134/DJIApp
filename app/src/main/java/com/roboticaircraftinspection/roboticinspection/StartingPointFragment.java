package com.roboticaircraftinspection.roboticinspection;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.roboticaircraftinspection.roboticinspection.models.InitializeTest;

import java.math.BigDecimal;

public class StartingPointFragment extends Fragment implements TestTimeline.OnInitializeTestListener{

    View view;
    Button nextButton;
    Button getPointButton;
    Activity activity;
    TestTimeline mTestTimeline;
    InitializeTest mInitializeTest;
    StartingPointFragment.OnStartingPointNextSelectedListener mCallback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_startingpoint, container, false);
        activity = getActivity();
        nextButton = view.findViewById(R.id.btn_next);

        TextView startLatitude = view.findViewById(R.id.starting_point_latitude);
        TextView startLongitude = view.findViewById(R.id.starting_point_longitude);
        startLatitude.setText(String.format(new BigDecimal(42.390370).toString(), "%f"));
        startLongitude.setText(String.format(new BigDecimal(-71.300740).toString(), "%f"));

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onStartingPointNextSelected();
            }
        });
        getPointButton = view.findViewById(R.id.get_point_button);
        getPointButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mTestTimeline.getStartingPoint();
            }
        });
        return view;
    }

    public void setOnStartingPointNextSelectedListener(Activity activity){
        mCallback = (StartingPointFragment.OnStartingPointNextSelectedListener)activity;
    }

    public interface OnStartingPointNextSelectedListener {
        void onStartingPointNextSelected();
    }
    public void setTimeline(TestTimeline testTimeline){
        mTestTimeline = testTimeline;
        mTestTimeline.setOnInitializeTestListener(this);
    }
    public void onInitializeTest(InitializeTest initializeTest){
        mInitializeTest = initializeTest;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView startLatitude = view.findViewById(R.id.starting_point_latitude);
                TextView startLongitude = view.findViewById(R.id.starting_point_longitude);
                if (mInitializeTest.startLatitude != 0){
                    startLatitude.setText(String.format(new BigDecimal(mInitializeTest.startLatitude).toString(), "%f"));
                }
                if (mInitializeTest.startLongitude != 0){
                    startLongitude.setText(String.format(new BigDecimal(mInitializeTest.startLongitude).toString(), "%f"));
                }

            }
        });
    }
}
