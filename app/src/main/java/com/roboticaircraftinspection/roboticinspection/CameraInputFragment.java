package com.roboticaircraftinspection.roboticinspection;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.roboticaircraftinspection.roboticinspection.models.CameraInput;

public class CameraInputFragment extends Fragment {
    View view;
    Button nextButton;
    OnAircraftInputNextSelectedListener mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_camera_input, container, false);
        nextButton = (Button) view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraInput cameraInput = new CameraInput();
                EditText CDist = getView().findViewById(R.id.CDist);
                cameraInput.CDist = CDist.getText().toString();
                EditText COZoom = getView().findViewById(R.id.COZoom);
                cameraInput.COZoom = COZoom.getText().toString();
                EditText CDZoom = getView().findViewById(R.id.CDZoom);
                cameraInput.CDZoom = CDZoom.getText().toString();
                mCallback.onAircraftInputNextSelected(cameraInput);
            }
        });
        return view;
    }
    public void setOnAircraftInputNextSelectedListener(Activity activity){
        mCallback = (OnAircraftInputNextSelectedListener)activity;
    }
    public interface OnAircraftInputNextSelectedListener {
        public void onAircraftInputNextSelected(CameraInput cameraInput);
    }

}
