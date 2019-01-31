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
import com.roboticaircraftinspection.roboticinspection.db.InspectionWaypoint;
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
    int aircraft_id;
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
                new ChooserDialog(view.getContext()).withChosenListener(new ChooserDialog.Result() {
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
                findAircraftByName();
            }
        });
        listView = view.findViewById(R.id.listView);
        itemArrayAdapter = new ItemArrayAdapter(view.getContext(), R.layout.item_layout);
        return view;
    }
    void saveWaypoints(Long aircraft_id){
        for (int i = 2; i < waypoints.size(); i++){
            String[] row = waypoints.get(i);
            InspectionWaypoint inspectionWaypoint = new InspectionWaypoint();
            inspectionWaypoint.setAircraft_id(aircraft_id.intValue());
            inspectionWaypoint.setX(Double.valueOf(row[2]));
            inspectionWaypoint.setY(Double.valueOf(row[3]));
            inspectionWaypoint.setAltitude(Double.valueOf(row[4]));
            inspectionWaypoint.setHeading(Double.valueOf(row[5]));
            SaveWaypoint saveWaypoint = new SaveWaypoint(this);
            saveWaypoint.execute(inspectionWaypoint);
        }

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
    private static class SaveAircraft extends AsyncTask<AircraftType, Void, Long> {
        private WeakReference<LoadCSVFragment> fragmentReference;
        SaveAircraft(LoadCSVFragment fragment){
            fragmentReference = new WeakReference<>(fragment);
        }
        @Override
        protected Long doInBackground(AircraftType...params){
            AircraftType aircraftType = params[0];
            return DatabaseClient.getInstance(MApplication.getContext()).getAppDatabase()
                    .aircraftDao()
                    .insert(aircraftType);
        }
        @Override
        protected void onPostExecute(Long aircraft_id){
            super.onPostExecute(aircraft_id);
            LoadCSVFragment fragment = fragmentReference.get();
            Log.d("NANCY", "Aircraft saved...Saving waypoints...aircraft_id: "+aircraft_id);
            fragment.saveWaypoints(aircraft_id);
        }
    }
    private void findAircraftByName(){
        LoadCSVFragment.FindAircraftByName findAircraftByName = new LoadCSVFragment.FindAircraftByName(this);
        findAircraftByName.execute(CSVFilePath);
    }
    private static class FindAircraftByName extends AsyncTask<String, Void, List<AircraftType>>{
        private WeakReference<LoadCSVFragment> fragmentReference;
        FindAircraftByName(LoadCSVFragment fragment){
            fragmentReference = new WeakReference<>(fragment);
        }
        @Override
        protected List<AircraftType> doInBackground(String...params){
            String name = params[0];
            Log.d("NANCY","Find aircraft: "+name);
            return DatabaseClient.getInstance(MApplication.getContext()).getAppDatabase()
                    .aircraftDao()
                    .findByName(name);
        }
        @Override
        protected void onPostExecute(List<AircraftType> aircraftTypes){
            super.onPostExecute(aircraftTypes);
            if (aircraftTypes == null){
                Log.d("NANCY", "Aircraft not found...saving aircraft");
                LoadCSVFragment fragment = fragmentReference.get();
                fragment.saveAircraft();
            } else
                Log.d("NANCY", "Aircraft found in database...not saving");
        }
    }
    private void saveWaypoint(
            int aircraft_id,
            double x,
            double y,
            double altitude,
            double heading)
    {
        InspectionWaypoint inspectionWaypoint = new InspectionWaypoint();
        inspectionWaypoint.setAircraft_id(aircraft_id);
        inspectionWaypoint.setX(x);
        inspectionWaypoint.setY(y);
        inspectionWaypoint.setAltitude(altitude);
        inspectionWaypoint.setHeading(heading);
        SaveWaypoint saveWaypoint = new SaveWaypoint(this);
        saveWaypoint.execute(inspectionWaypoint);
    }

    private static class SaveWaypoint extends AsyncTask<InspectionWaypoint, Void, Long> {
        private WeakReference<LoadCSVFragment> fragmentReference;
        SaveWaypoint(LoadCSVFragment fragment){
            fragmentReference = new WeakReference<>(fragment);
        }
        @Override
        protected Long doInBackground(InspectionWaypoint...params){
            InspectionWaypoint inspectionWaypoint = params[0];
            return DatabaseClient.getInstance(MApplication.getContext()).getAppDatabase()
                    .inspectionWaypointDao()
                    .insert(inspectionWaypoint);
        }
        @Override
        protected void onPostExecute(Long id){
            super.onPostExecute(id);
            Log.d("NANCY", "Waypoint saved. id: "+id);
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
