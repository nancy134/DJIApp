package com.roboticaircraftinspection.roboticinspection;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.roboticaircraftinspection.roboticinspection.db.AircraftType;
import com.roboticaircraftinspection.roboticinspection.db.DatabaseClient;
import com.roboticaircraftinspection.roboticinspection.db.InspectionWaypoint;
import com.roboticaircraftinspection.roboticinspection.rest.AircraftRemote;
import com.roboticaircraftinspection.roboticinspection.rest.Api;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AircraftFragment extends Fragment {
    View view;
    Button nextButton;
    Button downloadButton;
    int aircraftCount;
    int aircraftIndex;
    ProgressBar progressBar;
    List<AircraftRemote> aircraftRemoteList;
    AircraftType selectedAircraftType;
    AircraftFragment.OnAircraftNextSelectedListener mCallback;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_aircraft, container, false);
        progressBar = view.findViewById(R.id.progress_aircraft);
        nextButton = view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AircraftType aircraftType = new AircraftType();
                Spinner aircraftSpinner = view.findViewById(R.id.spinner_aircraft);
                Log.d("NANCY", "selectedItem: "+aircraftSpinner.getSelectedItem());
                selectedAircraftType = (AircraftType)aircraftSpinner.getSelectedItem();
                readWaypoints(selectedAircraftType.getId());
            }
        });
        downloadButton = view.findViewById(R.id.btn_download);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                downloadData();
            }
        });
        return view;
    }
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        readData();
    }
    private void readWaypoints(Integer aircraft_id){
        ReadWaypoints readWaypoints = new ReadWaypoints(this);
        readWaypoints.execute(aircraft_id);
    }
    private static class ReadWaypoints extends AsyncTask<Integer, Void, List<InspectionWaypoint>>{
        private WeakReference<AircraftFragment> fragmentReference;
        ReadWaypoints(AircraftFragment fragment){
            fragmentReference = new WeakReference<>(fragment);
        }
        @Override
        protected List<InspectionWaypoint> doInBackground(Integer...params){
            int aircraft_id = params[0];
            return DatabaseClient
                    .getInstance(MApplication.getContext())
                    .getAppDatabase()
                    .inspectionWaypointDao()
                    .findWaypointsById(aircraft_id);
        }
        @Override
        protected void onPostExecute(List<InspectionWaypoint> waypoints){
            super.onPostExecute(waypoints);
            AircraftFragment fragment = fragmentReference.get();
            TextView headingTextView = fragment.view.findViewById(R.id.heading);
            String headingString = headingTextView.getText().toString();
            Log.d("NANCY","headingString: "+headingString);
            double heading = Double.parseDouble(headingString);
            Log.d("NANCY", "heading: "+heading);
            fragment.mCallback.onAircraftNextSelected(fragment.selectedAircraftType, waypoints, heading);
        }
    }
    private void readData(){
        ReadData readData = new ReadData(this);
        readData.execute();
    }
    private static class ReadData extends AsyncTask<Void, Void, List<AircraftType>> {
        private WeakReference<AircraftFragment> fragmentReference;
        ReadData(AircraftFragment fragment){
            fragmentReference = new WeakReference<>(fragment);
        }
        @Override
        protected List<AircraftType> doInBackground(Void... voids){
            return DatabaseClient
                    .getInstance(MApplication.getContext())
                    .getAppDatabase()
                    .aircraftDao()
                    .getAll();
        }
        @Override
        protected void onPostExecute(List<AircraftType> aircraftTypes) {
            super.onPostExecute(aircraftTypes);
            AircraftFragment fragment = fragmentReference.get();
            fragment.updateSpinner(aircraftTypes);
            Log.d("NANCY", "Data read");
            Log.d("NANCY", "size: " + aircraftTypes.size());
        }
    }
    private void deleteData(){
        DeleteAll deleteAll = new DeleteAll(this);
        deleteAll.execute();
    }
    private static class DeleteAll extends AsyncTask<Void, Void, Void> {
        private WeakReference<AircraftFragment> fragmentReference;
        DeleteAll(AircraftFragment fragment){
            fragmentReference = new WeakReference<>(fragment);
        }
        @Override
        protected Void doInBackground(Void...voids){
            DatabaseClient.getInstance(MApplication.getContext()).getAppDatabase()
                .aircraftDao()
                .deleteAll();
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid){
            super.onPostExecute(aVoid);
            Log.d("NANCY", "Data deleted");
            AircraftFragment fragment = fragmentReference.get();
            fragment.saveAll();
        }
    }
    private void downloadData(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Api api = retrofit.create(Api.class);
        Call<List<AircraftRemote>> call = api.getAircraft();

        call.enqueue(new Callback<List<AircraftRemote>>() {
            @Override
            public void onResponse(@NonNull  Call<List<AircraftRemote>> call, @NonNull Response<List<AircraftRemote>> response) {
                aircraftRemoteList = response.body();
                deleteData();
            }

            @Override
            public void onFailure(@NonNull Call<List<AircraftRemote>> call, @NonNull Throwable t) {
                Log.d("NANCY",t.getMessage());
            }
        });
    }
    private void saveAll(){
        if (aircraftRemoteList != null) {
            Log.d("NANCY", "Data downloaded");
            aircraftCount = aircraftRemoteList.size();
            aircraftIndex = 0;
            for (int i = 0; i < aircraftRemoteList.size(); i++) {
                saveData(
                        aircraftRemoteList.get(i).getId(),
                        aircraftRemoteList.get(i).getName(),
                        aircraftRemoteList.get(i).getHeading(),
                        aircraftRemoteList.get(i).getLatitude(),
                        aircraftRemoteList.get(i).getLongitude());
            }
        }
    }
    private void saveData(
            int id,
            String name,
            double heading,
            double latitude,
            double longitude)
 {
        AircraftType aircraftType = new AircraftType();
        aircraftType.setId(id);
        aircraftType.setName(name);
        aircraftType.setHeading(heading);
        aircraftType.setLatitude(latitude);
        aircraftType.setLongitude(longitude);
        SaveAircraft saveAircraft = new SaveAircraft(this);
        saveAircraft.execute(aircraftType);
    }
    private static class SaveAircraft extends AsyncTask<AircraftType, Void, Void> {
        private WeakReference<AircraftFragment> fragmentReference;
        SaveAircraft(AircraftFragment fragment){
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
            AircraftFragment fragment = fragmentReference.get();
            fragment.aircraftIndex++;
            Log.d("NANCY","aircraftIndex: "+fragment.aircraftIndex);
            Log.d("NANCY","aircraftCount: "+fragment.aircraftCount);
            Log.d("NANCY", "Saved: "+fragment.aircraftIndex);
            if (fragment.aircraftCount == fragment.aircraftIndex) {

                Log.d("NANCY", "Save finished");
                fragment.readData();
            }
        }
    }
    private void updateSpinner(List<AircraftType> aircraftTypes){
        Log.d("NANCY","Update spinner");
        for (int i=0; i<aircraftTypes.size(); i++){
            Log.d("NANCY", "name: "+aircraftTypes.get(i).getName());
        }
        Spinner aircraftType = view.findViewById(R.id.spinner_aircraft);
        ArrayAdapter<AircraftType> spinnerAdapter = new ArrayAdapter<>(
                view.getContext(),
                R.layout.support_simple_spinner_dropdown_item,
                aircraftTypes);
        aircraftType.setAdapter(spinnerAdapter);
        spinnerAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);

    }
    public void setOnAircraftNextSelectedListener(Activity activity){
        mCallback = (AircraftFragment.OnAircraftNextSelectedListener)activity;
    }
    public interface OnAircraftNextSelectedListener {
        void onAircraftNextSelected(AircraftType aircraft, List<InspectionWaypoint> waypoints, double heading);
    }
}
