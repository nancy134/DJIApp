package com.roboticaircraftinspection.roboticinspection;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
    OnCameraInputNextSelectedListener mCallback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_camera_input, container, false);
        nextButton = view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraInput cameraInput = new CameraInput();
                EditText CDist = view.findViewById(R.id.CDist);
                cameraInput.CDist = CDist.getText().toString();
                EditText COZoom = view.findViewById(R.id.COZoom);
                cameraInput.COZoom = COZoom.getText().toString();
                EditText CDZoom = view.findViewById(R.id.CDZoom);
                cameraInput.CDZoom = CDZoom.getText().toString();
                mCallback.onCameraInputNextSelected(cameraInput);
            }
        });
        return view;
    }
    public void setOnCameraInputNextSelectedListener(Activity activity){
        mCallback = (OnCameraInputNextSelectedListener)activity;
    }
    public interface OnCameraInputNextSelectedListener {
        void onCameraInputNextSelected(CameraInput cameraInput);
    }

}
