package com.roboticaircraftinspection.roboticinspection;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.roboticaircraftinspection.roboticinspection.models.TaskType;


public class TaskSelectionFragment extends Fragment {
    View view;
    Button nextButton;
    RadioGroup taskGroup;
    RadioButton taskLoadCSV;
    RadioButton taskRunMission;

    TaskSelectionFragment.OnTaskSelectionNextSelectedListener mCallback;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_taskselection, container, false);

        nextButton = view.findViewById(R.id.btn_next);
        taskGroup = view.findViewById(R.id.radioTask);
        taskLoadCSV = view.findViewById(R.id.radioLoadCSV);
        taskRunMission = view.findViewById(R.id.radioRunMission);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskType selectedTaskType = null;
                int selectedId = taskGroup.getCheckedRadioButtonId();
                if (selectedId == R.id.radioLoadCSV) {
                    selectedTaskType = TaskType.LOAD_WAYPOINTS;
                } else if (selectedId == R.id.radioRunMission) {
                    selectedTaskType = TaskType.INSPECT_AIRCRAFT;
                }
                mCallback.onTaskSelectionNextSelected(selectedTaskType);
            }
        });
        return view;
    }
    public void setOnTaskSelectionNextSelectedListener(Activity activity){
        mCallback = (TaskSelectionFragment.OnTaskSelectionNextSelectedListener)activity;
    }
    public interface OnTaskSelectionNextSelectedListener {
        void onTaskSelectionNextSelected(TaskType taskType);
    }
}
