package com.roboticaircraftinspection.roboticinspection;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.roboticaircraftinspection.roboticinspection.models.AcModels;
import com.roboticaircraftinspection.roboticinspection.models.MissionOptions;
import com.roboticaircraftinspection.roboticinspection.models.MissionType;
import com.roboticaircraftinspection.roboticinspection.models.StartMission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MissionOptionsFragment extends Fragment {

    View view;
    Button nextButton;
    OnOptionsNextSelectedListener mCallback;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mission_options, container, false);

        Spinner missionTypeSpinner = view.findViewById(R.id.spinner_mission_type);
        //List<String> missionTypeList = new ArrayList<String>();
        //for ( final MissionType missionType : MissionType.values() )
        //{
        //    missionTypeList.add( getString( missionType.id() ) );
        //}
        final ArrayAdapter<MissionType> missionListAdapter = new ArrayAdapter<MissionType>(
                view.getContext(),
                R.layout.support_simple_spinner_dropdown_item,
                MissionType.values()
        );
        missionTypeSpinner.setAdapter(missionListAdapter);
        missionListAdapter.notifyDataSetChanged();

        Spinner aircraftType = view.findViewById(R.id.spinner_aircraft_type);
        List<String> aircraftList = Arrays.asList(AcModels.ACMODELS);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                view.getContext(),
                R.layout.support_simple_spinner_dropdown_item,
                aircraftList);
        aircraftType.setAdapter(spinnerAdapter);
        spinnerAdapter.notifyDataSetChanged();

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
                for (StartMission s: StartMission.values()){
                    if (s.id() == radioGroup.getCheckedRadioButtonId()) missionOptions.startMission = s;
                }

                Spinner aircraftType = getView().findViewById(R.id.spinner_aircraft_type);
                missionOptions.aircraftType = aircraftType.getSelectedItem().toString();

                Spinner missionType = getView().findViewById(R.id.spinner_mission_type);
                for (MissionType m: MissionType.values()){
                    if (m.position() == missionType.getSelectedItemId()){
                        missionOptions.missionType = m;
                    }
                }
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
