package com.roboticaircraftinspection.roboticinspection;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.roboticaircraftinspection.roboticinspection.models.CameraInput;
import com.roboticaircraftinspection.roboticinspection.models.MissionOptions;
import com.roboticaircraftinspection.roboticinspection.models.MissionType;
import com.roboticaircraftinspection.roboticinspection.models.OtherEndInput;
import com.roboticaircraftinspection.roboticinspection.models.StartMission;

public class CreateMissionActivity extends AppCompatActivity
    implements MissionOptionsFragment.OnOptionsNextSelectedListener,
        CameraInputFragment.OnCameraInputNextSelectedListener,
        OtherEndInputFragment.OnOtherEndInputNextSelectedListener,
        InitializeZonesFragment.OnInitializeNextSelectedListener,
        HomePointFragment.OnHomePointNextSelectedListener
{

    MissionOptions mMissionOptions;
    CameraInput mCameraInput;
    ZonesTimeline mZonesTimeline;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_mission);
        MissionOptionsFragment missionOptionsFragment = new MissionOptionsFragment();
        missionOptionsFragment.setOnOptionsNextSelectedListener(this);
        loadFragment(missionOptionsFragment);

    }
    public void onOptionsNextSelected(MissionOptions options){
        mMissionOptions = new MissionOptions(options);
        if (mMissionOptions.missionType.id() == MissionType.POINTS_OF_INTEREST.id()) {
            OtherEndInputFragment otherEndInputFragment = new OtherEndInputFragment();
            otherEndInputFragment.setOnOtherEndInputNextSelectedListener(this);
            loadFragment(otherEndInputFragment);
        } else if (!mMissionOptions.aircraftType.equals("OTHER")) {

            CameraInputFragment cameraInputFragment = new CameraInputFragment();
            cameraInputFragment.setOnCameraInputNextSelectedListener(this);
            loadFragment(cameraInputFragment);
        }
    }

    @Override
    public void onCameraInputNextSelected(CameraInput cameraInput) {
        mCameraInput = cameraInput;
        boolean isFromTailEnd = false;
        if (mMissionOptions.startMission == StartMission.FROM_TAIL){
            isFromTailEnd = true;
        }
        String mediaType;
        if (mMissionOptions.photo && mMissionOptions.video) mediaType = "BOTH";
        else if (mMissionOptions.photo) mediaType = "PHOTO";
        else if (mMissionOptions.video) mediaType = "VIDEO";
        else mediaType = "BOTH";
        if (mCameraInput.CDist.length() == 0) mCameraInput.CDist = "0";
        if (mCameraInput.COZoom.length() == 0) mCameraInput.COZoom = "0";
        if (mCameraInput.CDZoom.length() == 0) mCameraInput.CDZoom = "0";

        CameraTestTimeline cameraTestTimeline = new CameraTestTimeline(
                mMissionOptions.aircraftType,
                isFromTailEnd,
                mediaType,
                Double.valueOf(mCameraInput.CDist),
                Double.valueOf(mCameraInput.COZoom),
                Double.valueOf(mCameraInput.CDZoom));
        finish();
    }
    @Override
    public void onOtherEndInputNextSelected(OtherEndInput otherEndInput) {
        boolean isGeoFenceEnabled = mMissionOptions.geofence;
        String mediaType = null;
        if (mMissionOptions.photo && mMissionOptions.video) mediaType = "BOTH";
        else if (mMissionOptions.photo) mediaType = "PHOTO";
        else if (mMissionOptions.video) mediaType = "VIDEO";
        double endLat;
        double endLong;
        if (otherEndInput.otherEndLongitude.length() > 0){
            endLat = Double.valueOf(otherEndInput.otherEndLatitude);
        }
        if (otherEndInput.otherEndLongitude.length() > 0){
            endLong = Double.valueOf(otherEndInput.otherEndLongitude);
        }
        boolean isFromTailEnd = false;
        if (mMissionOptions.startMission == StartMission.FROM_TAIL){
            isFromTailEnd = true;
        }
        endLat = 42.390370;
        endLong = -71.300740;
        mZonesTimeline = new ZonesTimeline(
                mMissionOptions.aircraftType,
                isFromTailEnd,
                mediaType,
                endLat,
                endLong,
                isGeoFenceEnabled);
        InitializeZonesFragment initializeFragment = new InitializeZonesFragment();
        initializeFragment.setTimeline(mZonesTimeline);
        initializeFragment.setOnInitializeNextSelectedListener(this);

        loadFragment(initializeFragment);
    }

    @Override
    public void onInitializeNextSelected() {
        HomePointFragment homePointFragment = new HomePointFragment();
        homePointFragment.setOnHomePointNextSelectedListener(this);
        homePointFragment.setTimeline(mZonesTimeline);
        loadFragment(homePointFragment);
    }

    @Override
    public void onHomePointNextSelected(){
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
