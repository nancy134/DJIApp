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
import android.widget.Button;

import com.roboticaircraftinspection.roboticinspection.db.AircraftType;
import com.roboticaircraftinspection.roboticinspection.db.DatabaseClient;
import com.roboticaircraftinspection.roboticinspection.rest.AircraftRemote;
import com.roboticaircraftinspection.roboticinspection.rest.Api;

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
    Button readButton;
    Button deleteButton;
    AircraftFragment.OnAircraftNextSelectedListener mCallback;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_aircraft, container, false);
        nextButton = view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AircraftType aircraftType = new AircraftType();
                mCallback.onAircraftNextSelected(aircraftType);
            }
        });
        downloadButton = view.findViewById(R.id.btn_download);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadData();
            }
        });
        readButton = view.findViewById(R.id.btn_read_data);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readData();
            }
        });
        deleteButton = view.findViewById(R.id.btn_delete_data);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteData();
            }
        });
        return view;
    }
    private static void readData(){
        class ReadData extends AsyncTask<Void, Void, List<AircraftType>> {
            @Override
            protected List<AircraftType> doInBackground(Void... voids){
                return DatabaseClient
                        .getInstance(MApplication.getContext())
                        .getAppDatabase()
                        .aircraftDao()
                        .getAll();
            }
            @Override
            protected void onPostExecute(List<AircraftType> aircraftTypes){
                super.onPostExecute(aircraftTypes);

                Log.d("NANCY","Data read");
                Log.d("NANCY", "size: "+aircraftTypes.size());
            }
        }
        ReadData readData = new ReadData();
        readData.execute();
    }
    private static void deleteData(){
        class DeleteAll extends AsyncTask<Void, Void, Void> {
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
                downloadData();
            }
        }
        DeleteAll deleteAll = new DeleteAll();
        deleteAll.execute();
    }
    private static void downloadData(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Api api = retrofit.create(Api.class);
        Call<List<AircraftRemote>> call = api.getAircraft();

        call.enqueue(new Callback<List<AircraftRemote>>() {
            @Override
            public void onResponse(@NonNull  Call<List<AircraftRemote>> call, @NonNull Response<List<AircraftRemote>> response) {
                List<AircraftRemote> aircraftRemoteList = response.body();
                if (aircraftRemoteList != null) {
                    Log.d("NANCY", "Data downloaded");
                    for (int i = 0; i < aircraftRemoteList.size(); i++) {
                        saveData(
                                aircraftRemoteList.get(i).getId(),
                                aircraftRemoteList.get(i).getName(),
                                aircraftRemoteList.get(i).getNoseLatitude(),
                                aircraftRemoteList.get(i).getNoseLongitude(),
                                aircraftRemoteList.get(i).getTailLatitude(),
                                aircraftRemoteList.get(i).getTailLongitude());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AircraftRemote>> call, @NonNull Throwable t) {
                Log.d("NANCY",t.getMessage());
            }
        });
    }
    private static void saveData(
            int id,
            String name,
            double noseLatitude,
            double noseLongitude,
            double tailLatitude,
            double tailLongitude){
        final  int downloadedId = id;
        final String downloadedName = name;
        final double downloadedNoseLatitude = noseLatitude;
        final double downloadedNoseLongitude = noseLongitude;
        final double downloadedTailLatitude = tailLatitude;
        final double downloadedTailLongitude = tailLongitude;
        class SaveAircraft extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void...voids){
                AircraftType aircraftType = new AircraftType();
                aircraftType.setId(downloadedId);
                aircraftType.setName(downloadedName);
                aircraftType.setNoseLatitude(downloadedNoseLatitude);
                aircraftType.setNoseLongitude(downloadedNoseLongitude);
                aircraftType.setTailLatitude(downloadedTailLatitude);
                aircraftType.setTailLongitude(downloadedTailLongitude);

                DatabaseClient.getInstance(MApplication.getContext()).getAppDatabase()
                        .aircraftDao()
                        .insert(aircraftType);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid){
                super.onPostExecute(aVoid);
                Log.d("NANCY", "Data saved");
            }
        }
        SaveAircraft saveAircraft = new SaveAircraft();
        saveAircraft.execute();
    }
    public void setOnAircraftNextSelectedListener(Activity activity){
        mCallback = (AircraftFragment.OnAircraftNextSelectedListener)activity;
    }
    public interface OnAircraftNextSelectedListener {
        void onAircraftNextSelected(AircraftType aircraft);
    }
}
