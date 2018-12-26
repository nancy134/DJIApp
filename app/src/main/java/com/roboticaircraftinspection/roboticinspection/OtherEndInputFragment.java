package com.roboticaircraftinspection.roboticinspection;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.roboticaircraftinspection.roboticinspection.models.OtherEndInput;

public class OtherEndInputFragment extends Fragment {
    View view;
    Button nextButton;
    OnOtherEndInputNextSelectedListener mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_other_end_input, container, false);
        nextButton = (Button) view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OtherEndInput otherEndInput = new OtherEndInput();
                EditText OtherEndLatitude = getView().findViewById(R.id.other_end_latitude);
                otherEndInput.otherEndLatitude = OtherEndLatitude.getText().toString();
                EditText OtherEndLongitude = getView().findViewById(R.id.other_end_longitude);
                otherEndInput.otherEndLongitude = OtherEndLongitude.getText().toString();
                mCallback.onOtherEndInputNextSelected(otherEndInput);
            }
        });
        return view;
    }
    public void setOnOtherEndInputNextSelectedListener(Activity activity){
        mCallback = (OnOtherEndInputNextSelectedListener)activity;
    }
    public interface OnOtherEndInputNextSelectedListener {
        public void onOtherEndInputNextSelected(OtherEndInput otherEndInput);
    }

}
