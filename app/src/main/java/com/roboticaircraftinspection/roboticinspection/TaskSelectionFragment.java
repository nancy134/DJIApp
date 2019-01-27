package com.roboticaircraftinspection.roboticinspection;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.roboticaircraftinspection.roboticinspection.models.TaskType;


public class TaskSelectionFragment extends Fragment {
    View view;
    Button nextButton;
    TaskSelectionFragment.OnTaskSelectionNextSelectedListener mCallback;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_taskselection, container, false);

        Spinner taskSpinner = view.findViewById(R.id.spinner_task);
        final ArrayAdapter<TaskType> taskListAdapter = new ArrayAdapter<>(
                view.getContext(),
                R.layout.support_simple_spinner_dropdown_item,
                TaskType.values()
        );
        taskSpinner.setAdapter(taskListAdapter);
        taskListAdapter.notifyDataSetChanged();

        nextButton = view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner taskType = view.findViewById(R.id.spinner_task);
                TaskType selectedTaskType = null;
                for (TaskType m: TaskType.values()){
                    if (m.position() == taskType.getSelectedItemId()){
                        selectedTaskType = m;
                    }
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
