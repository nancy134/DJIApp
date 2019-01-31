package com.roboticaircraftinspection.roboticinspection;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.roboticaircraftinspection.roboticinspection.db.AircraftType;
import com.roboticaircraftinspection.roboticinspection.db.DatabaseClient;
import com.roboticaircraftinspection.roboticinspection.utils.CSVFile;
import com.roboticaircraftinspection.roboticinspection.utils.ItemArrayAdapter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class LoadCSVFragment extends Fragment {
    View view;
    Button nextButton;
    Button loadCSVButton;
    Button saveButton;
    ListView listView;
    ItemArrayAdapter itemArrayAdapter;
    List<String[]> waypoints;
    String CSVFilePath;
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
                new ChooserDialog().with(view.getContext()).withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        CSVFilePath = path;
                        CSVFile csvFile = new CSVFile(path);
                        waypoints = csvFile.read();
                        for(String[] waypointData:waypoints ) {
                            itemArrayAdapter.add(waypointData);
                        }
                        listView.setAdapter(itemArrayAdapter);
                    }
                }).build().show();
            }
        });
        saveButton = view.findViewById(R.id.save_to_database);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAircraft();
            }
        });
        listView = view.findViewById(R.id.listView);
        itemArrayAdapter = new ItemArrayAdapter(view.getContext(), R.layout.item_layout);
        return view;
    }
    private void saveAircraft(){
        if (waypoints.size() >= 2) {
            String[] secondRow = waypoints.get(1);
            AircraftType aircraftType = new AircraftType();
            aircraftType.setName(CSVFilePath);
            aircraftType.setHeading(Double.valueOf(secondRow[5]));
            aircraftType.setLatitude(Double.valueOf(secondRow[0]));
            aircraftType.setLongitude(Double.valueOf(secondRow[1]));
            LoadCSVFragment.SaveAircraft saveAircraft = new LoadCSVFragment.SaveAircraft(this);
            saveAircraft.execute(aircraftType);
        }
    }
    private static class SaveAircraft extends AsyncTask<AircraftType, Void, Void> {
        private WeakReference<LoadCSVFragment> fragmentReference;
        SaveAircraft(LoadCSVFragment fragment){
            fragmentReference = new WeakReference<>(fragment);
        }
        @Override
        protected Void doInBackground(AircraftType...params){
            AircraftType aircraftType = params[0];
            DatabaseClient.getInstance(MApplication.getContext()).getAppDatabase()
                    .aircraftDao()
                    .insert(aircraftType);
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid){
            super.onPostExecute(aVoid);
        }
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
