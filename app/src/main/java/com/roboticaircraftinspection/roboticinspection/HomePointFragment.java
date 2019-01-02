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

import com.roboticaircraftinspection.roboticinspection.models.HomePoint;

import java.math.BigDecimal;

public class HomePointFragment extends Fragment implements ZonesTimeline.OnHomePointListener{
    View view;
    Button nextButton;
    HomePointFragment.OnHomePointNextSelectedListener mCallback;
    ZonesTimeline mZonesTimeline;
    HomePoint mHomePoint;
    Activity activity;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_homepoint, container, false);
        activity = getActivity();
        nextButton = view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onHomePointNextSelected();
            }
        });
        return view;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mZonesTimeline.getHomepoint();
        mZonesTimeline.initTimeline();
    }
    public void setTimeline(ZonesTimeline zonesTimeline){
        mZonesTimeline = zonesTimeline;
        mZonesTimeline.setOnHomePointListener(this);
    }

    public void setOnHomePointNextSelectedListener(Activity activity){
        mCallback = (HomePointFragment.OnHomePointNextSelectedListener)activity;
    }
    public interface OnHomePointNextSelectedListener {
        void onHomePointNextSelected();
    }

    public void onHomePoint(HomePoint homePoint){
        mHomePoint = homePoint;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView homePointLatitudeCheck = view.findViewById(R.id.homepoint_latitude_check);
                TextView homePointLatitudeLabel = view.findViewById(R.id.homepoint_latitude_label);
                TextView homePointLatitudeValue = view.findViewById(R.id.homepoint_latitude_value);
                if (mHomePoint.latitude != 0){
                    homePointLatitudeCheck.setEnabled(true);
                    homePointLatitudeLabel.setEnabled(true);
                    homePointLatitudeValue.setText(String.format(new BigDecimal(mHomePoint.latitude).toString(), "%f"));
                } else {
                    homePointLatitudeCheck.setEnabled(false);
                    homePointLatitudeLabel.setEnabled(false);
                }
                TextView homePointLongitudeCheck = view.findViewById(R.id.homepoint_longitude_check);
                TextView homePointLongitudeLabel = view.findViewById(R.id.homepoint_longitude_label);
                TextView homePointLongitudeValue = view.findViewById(R.id.homepoint_longitude_value);
                if (mHomePoint.latitude != 0){
                    homePointLongitudeCheck.setEnabled(true);
                    homePointLongitudeLabel.setEnabled(true);
                    homePointLongitudeValue.setText(String.format(new BigDecimal(mHomePoint.longitude).toString(), "%f"));
                } else {
                    homePointLongitudeCheck.setEnabled(false);
                    homePointLongitudeLabel.setEnabled(false);
                }
            }
        });
    }
}
