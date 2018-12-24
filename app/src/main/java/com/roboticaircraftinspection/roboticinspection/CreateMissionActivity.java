package com.roboticaircraftinspection.roboticinspection;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.roboticaircraftinspection.roboticinspection.models.CameraInput;
import com.roboticaircraftinspection.roboticinspection.models.MissionOptions;
import com.roboticaircraftinspection.roboticinspection.models.StartMission;

public class CreateMissionActivity extends AppCompatActivity
    implements MissionOptionsFragment.OnOptionsNextSelectedListener, CameraInputFragment.OnAircraftInputNextSelectedListener{

    MissionOptions mMissionOptions;
    CameraInput mCameraInput;
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

        CameraInputFragment cameraInputFragment = new CameraInputFragment();
        cameraInputFragment.setOnAircraftInputNextSelectedListener(this);
        loadFragment(cameraInputFragment);
    }

    @Override
    public void onAircraftInputNextSelected(CameraInput cameraInput) {
        mCameraInput = cameraInput;
        //public void CameraTestTimeline(
        //        String acModel,
        //boolean isFromTailEnd,
        //String mediaType,
        //double cDist,
        //double cOZoom ,
        //double cDZoom)
        boolean isFromTailEnd = false;
        if (mMissionOptions.startMission == StartMission.FROM_TAIL){
            isFromTailEnd = true;
        }
        String mediaType = "BOTH";
        if (mMissionOptions.photo && mMissionOptions.video) mediaType = "BOTH";
        else if (mMissionOptions.photo) mediaType = "PHOTO";
        else if (mMissionOptions.video) mediaType = "VIDEO";

        if (!mMissionOptions.aircraftType.equals("OTHER")) {
            CameraTestTimeline cameraTestTimeline = new CameraTestTimeline(
                    mMissionOptions.aircraftType,
                    isFromTailEnd,
                    mediaType,
                    Double.valueOf(mCameraInput.CDist),
                    Double.valueOf(mCameraInput.COZoom),
                    Double.valueOf(mCameraInput.CDZoom));
        }
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
