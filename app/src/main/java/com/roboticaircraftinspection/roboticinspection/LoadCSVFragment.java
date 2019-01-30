package com.roboticaircraftinspection.roboticinspection;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.obsez.android.lib.filechooser.ChooserDialog;
import com.roboticaircraftinspection.roboticinspection.utils.CSVFile;
import com.roboticaircraftinspection.roboticinspection.utils.ItemArrayAdapter;

import java.io.File;
import java.util.List;

public class LoadCSVFragment extends Fragment {
    View view;
    Button nextButton;
    Button loadCSVButton;
    ListView listView;
    ItemArrayAdapter itemArrayAdapter;
    int FILE_REQUEST_CODE = 0;
    LoadCSVFragment.OnLoadCSVNextSelectedListener mCallback;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_loadcsv, container, false);
        nextButton = view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onLoadCSVNextSelected();
            }
        });
        loadCSVButton = view.findViewById(R.id.btn_load_csv);
        loadCSVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ChooserDialog().with(view.getContext())
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String path, File pathFile) {
                                Log.d("NANCY", "path: "+path);
                                CSVFile csvFile = new CSVFile(path);
                                List<String[]> waypoints = csvFile.read();
                                Log.d("NANCY", "waypoints length: "+waypoints.get(0).length);
                                for(String[] waypointData:waypoints ) {
                                    itemArrayAdapter.add(waypointData);
                                }
                                listView.setAdapter(itemArrayAdapter);
                           }
                        })
                        .build()
                        .show();
                /*
                Intent intent = new Intent(view.getContext(), FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.CONFIGS,new Configurations.Builder()
                        .setShowImages(false)
                        .setShowVideos(false)
                        .setShowFiles(true)
                        .setSuffixes("csv")
                        .setSingleChoiceMode(true).build());

                startActivityForResult(intent, FILE_REQUEST_CODE);
                */
            }
        });
        listView = view.findViewById(R.id.listView);
        itemArrayAdapter = new ItemArrayAdapter(view.getContext(), R.layout.item_layout);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST_CODE){
            Log.d("NANCY","FILE_REQUEST_CODE");
        }
    }

    public void setOnLoadCSVNextSelectedListener(Activity activity){
        mCallback = (LoadCSVFragment.OnLoadCSVNextSelectedListener)activity;
    }
    public interface OnLoadCSVNextSelectedListener {
        void onLoadCSVNextSelected();
    }
}
