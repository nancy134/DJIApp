package com.roboticaircraftinspection.roboticinspection;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.roboticaircraftinspection.roboticinspection.models.MissionOptions;

public class MissionOptionsFragment extends Fragment {

    View view;
    Button nextButton;
    OnOptionsNextSelectedListener mCallback;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mission_options, container, false);

        nextButton = (Button) view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MissionOptions missionOptions = new MissionOptions();
                CheckBox photo = getView().findViewById(R.id.photo);
                missionOptions.photo = photo.isChecked();
                CheckBox video = getView().findViewById(R.id.video);
                missionOptions.video = video.isChecked();
                CheckBox passes = getView().findViewById(R.id.passes);
                missionOptions.multiplePasses = passes.isChecked();
                CheckBox geofence = getView().findViewById(R.id.geofence);
                missionOptions.geofence = geofence.isChecked();
                RadioGroup radioGroup = getView().findViewById(R.id.startgroup);
                missionOptions.startMission = radioGroup.getCheckedRadioButtonId();
                Spinner aircraftType = getView().findViewById(R.id.spinner_aircraft_type);
                missionOptions.aircraftType = aircraftType.getSelectedItemId();
                Spinner missionType = getView().findViewById(R.id.spinner_mission_type);
                missionOptions.missionType = missionType.getSelectedItemId();
                mCallback.onOptionsNextSelected(missionOptions);

            }
        });
        return view;
    }
    public void setOnOptionsNextSelectedListener(Activity activity){
        mCallback = (OnOptionsNextSelectedListener)activity;
    }
    public interface OnOptionsNextSelectedListener {
        public void onOptionsNextSelected(MissionOptions options);
    }
}
