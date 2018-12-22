package com.roboticaircraftinspection.roboticinspection;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.roboticaircraftinspection.roboticinspection.models.AircraftInput;

public class AircraftInputFragment extends Fragment {
    View view;
    Button nextButton;
    OnAircraftInputNextSelectedListener mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_aircraft_input, container, false);
        nextButton = (Button) view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AircraftInput aircraftInput = new AircraftInput();
                EditText CDist = getView().findViewById(R.id.CDist);
                aircraftInput.CDist = CDist.getText().toString();
                EditText COZoom = getView().findViewById(R.id.COZoom);
                aircraftInput.COZoom = COZoom.getText().toString();
                EditText CDZoom = getView().findViewById(R.id.CDZoom);
                aircraftInput.CDZoom = CDZoom.getText().toString();
                mCallback.onAircraftInputNextSelected(aircraftInput);
            }
        });
        return view;
    }
    public void setOnAircraftInputNextSelectedListener(Activity activity){
        mCallback = (OnAircraftInputNextSelectedListener)activity;
    }
    public interface OnAircraftInputNextSelectedListener {
        public void onAircraftInputNextSelected(AircraftInput aircraftInput);
    }

}
