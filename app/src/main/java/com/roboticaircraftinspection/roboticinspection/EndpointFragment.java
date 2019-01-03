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

public class EndpointFragment extends Fragment implements TestTimeline.OnInitializeTestListener{

    View view;
    Button nextButton;
    Button getPointButton;
    Activity activity;
    TestTimeline mTestTimeline;
    InitializeTest mInitializeTest;
    EndpointFragment.OnEndpointNextSelectedListener mCallback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_endpoint, container, false);
        activity = getActivity();
        nextButton = view.findViewById(R.id.btn_next);

        TextView endLatitude = view.findViewById(R.id.endpoint_latitude);
        TextView endLongitude = view.findViewById(R.id.endpoint_longitude);
        endLatitude.setText(String.format(new BigDecimal(42.390370).toString(), "%f"));
        endLongitude.setText(String.format(new BigDecimal(-71.300740).toString(), "%f"));

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onEndpointNextSelected();
            }
        });
        getPointButton = view.findViewById(R.id.get_point_button);
        getPointButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mTestTimeline.getEndPoint();
            }
        });
        return view;
    }

    public void setOnEndpointNextSelectedListener(Activity activity){
        mCallback = (EndpointFragment.OnEndpointNextSelectedListener)activity;
    }

    public interface OnEndpointNextSelectedListener {
        void onEndpointNextSelected();
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
                TextView endLatitude = view.findViewById(R.id.endpoint_latitude);
                TextView endLongitude = view.findViewById(R.id.endpoint_longitude);
                if (mInitializeTest.endLatitude != 0){
                    endLatitude.setText(String.format(new BigDecimal(mInitializeTest.endLatitude).toString(), "%f"));
                }
                if (mInitializeTest.endLongitude != 0){
                    endLongitude.setText(String.format(new BigDecimal(mInitializeTest.endLongitude).toString(), "%f"));
                }

            }
        });
    }
}
