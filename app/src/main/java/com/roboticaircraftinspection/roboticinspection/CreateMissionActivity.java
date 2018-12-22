package com.roboticaircraftinspection.roboticinspection;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.roboticaircraftinspection.roboticinspection.models.AircraftInput;
import com.roboticaircraftinspection.roboticinspection.models.MissionOptions;

public class CreateMissionActivity extends AppCompatActivity
    implements MissionOptionsFragment.OnOptionsNextSelectedListener, AircraftInputFragment.OnAircraftInputNextSelectedListener{


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_mission);
        MissionOptionsFragment missionOptionsFragment = new MissionOptionsFragment();
        missionOptionsFragment.setOnOptionsNextSelectedListener(this);
        loadFragment(missionOptionsFragment);

    }
    public void onOptionsNextSelected(MissionOptions options){
        AircraftInputFragment aircraftInputFragment = new AircraftInputFragment();
        aircraftInputFragment.setOnAircraftInputNextSelectedListener(this);
        loadFragment(aircraftInputFragment);
    }

    @Override
    public void onAircraftInputNextSelected(AircraftInput aircraftInput) {
        finish();
    }

    private void loadFragment(Fragment fragment) {
        // create a FragmentManager
        FragmentManager fm = getSupportFragmentManager();
        // create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        // replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.createMissionLayout, fragment);
        fragmentTransaction.commit(); // save the changes
    }
}
