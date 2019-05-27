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
    int waypointCount;
    int waypointIndex;
    LoadCSVFragment.OnLoadCSVNextSelectedListener mCallback;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_loadcsv, container, false);
        nextButton = view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findAircraftByName();
                //mCallback.onLoadCSVNextSelected();
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
                        String[] heading = waypoints.get(0);
                        int indexX=0, indexY=0, indexAltitude=0, indexHeading=0, indexGimbalPitch=0;
                        for (int i = 0; i< heading.length; i++){
                            if (heading[i].toLowerCase().contains("heading")){
                                indexHeading = i;
                            } else if (heading[i].toLowerCase().contains("altitude")){
                                indexAltitude = i;
                            } else if (heading[i].toLowerCase().contains("x lat")){
                                indexX = i;
                            } else if (heading[i].toLowerCase().contains("y lon")){
                                indexY = i;
                            } else if (heading[i].toLowerCase().contains("gimbalpitchangle")){
                                indexGimbalPitch = i;
                            }
                        }
                        for (int i = 0; i< waypoints.size(); i++){
                            String[] waypointData = new String[5];
                            waypointData[0] = waypoints.get(i)[indexX];
                            waypointData[1] = waypoints.get(i)[indexY];
                            waypointData[2] = waypoints.get(i)[indexAltitude];
                            waypointData[3] = waypoints.get(i)[indexHeading];
                            waypointData[4] = waypoints.get(i)[indexGimbalPitch];
                            itemArrayAdapter.add(waypointData);

                        }
                        //for(String[] waypointData:waypoints ) {
                        //    itemArrayAdapter.add(waypointData);
                        //}
                        listView.setAdapter(itemArrayAdapter);
                    }
                }).build().show();
            }
        });
        saveButton = view.findViewById(R.id.save_to_database);
        saveButton.setVisibility(View.GONE);
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
        waypointCount = waypoints.size()-2;
        waypointIndex = 0;
		String[] heading = waypoints.get(0);
		int indexX=0, indexY=0, indexAltitude=0, indexHeading=0, indexGimbalPitch=0;
		for (int i = 0; i< heading.length; i++){
			if (heading[i].toLowerCase().contains("heading")){
				indexHeading = i;
			} else if (heading[i].toLowerCase().contains("altitude")){
				indexAltitude = i;
			} else if (heading[i].toLowerCase().contains("x lat")){
				indexX = i;
			} else if (heading[i].toLowerCase().contains("y lon")){
				indexY = i;
			} else if (heading[i].toLowerCase().contains("gimbalpitchangle")){
			    indexGimbalPitch = i;
            }
		}
        for (int i = 2; i < waypoints.size(); i++){
            String[] row = waypoints.get(i);
            InspectionWaypoint inspectionWaypoint = new InspectionWaypoint();
            inspectionWaypoint.setAircraft_id(aircraft_id.intValue());
            inspectionWaypoint.setX(Double.valueOf(row[indexX]));
            inspectionWaypoint.setY(Double.valueOf(row[indexY]));
            inspectionWaypoint.setAltitude(Double.valueOf(row[indexAltitude]));
            inspectionWaypoint.setHeading(Double.valueOf(row[indexHeading]));
            inspectionWaypoint.setGimbalPitch(Integer.parseInt(row[indexGimbalPitch]));
            SaveWaypoint saveWaypoint = new SaveWaypoint(this);
            saveWaypoint.execute(inspectionWaypoint);
        }

    }
    private void saveAircraft(){
        if (waypoints.size() >= 2) {
            String[] secondRow = waypoints.get(1);
            AircraftType aircraftType = new AircraftType();
            aircraftType.setName(CSVFilePath);
            aircraftType.setHeading(Double.valueOf(secondRow[3]));
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
            Log.d("NANCY","size: "+aircraftTypes.size());
            LoadCSVFragment fragment = fragmentReference.get();
            if (aircraftTypes == null || aircraftTypes.size() == 0){
                Log.d("NANCY", "Aircraft not found...saving aircraft");
                fragment.saveAircraft();
            } else
                fragment.mCallback.onLoadCSVNextSelected();
                Log.d("NANCY", "Aircraft found in database...not saving");
        }
    }
    private void saveWaypoint(
            int aircraft_id,
            double x,
            double y,
            double altitude,
            double heading,
            int gimbalPitch)
    {
        InspectionWaypoint inspectionWaypoint = new InspectionWaypoint();
        inspectionWaypoint.setAircraft_id(aircraft_id);
        inspectionWaypoint.setX(x);
        inspectionWaypoint.setY(y);
        inspectionWaypoint.setAltitude(altitude);
        inspectionWaypoint.setHeading(heading);
        inspectionWaypoint.setGimbalPitch(gimbalPitch);
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
            LoadCSVFragment fragment = fragmentReference.get();
            fragment.waypointIndex++;
            Log.d("NANCY","waypointCount: "+fragment.waypointCount+" waypointIndex: "+fragment.waypointIndex);
            if (fragment.waypointCount == fragment.waypointIndex){
                fragment.mCallback.onLoadCSVNextSelected();
            }
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
