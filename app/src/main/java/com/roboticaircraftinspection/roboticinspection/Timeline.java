package com.roboticaircraftinspection.roboticinspection;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dji.common.error.DJIError;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.Triggerable;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.timeline.TimelineMission;
import dji.sdk.mission.timeline.triggers.AircraftLandedTrigger;
import dji.sdk.mission.timeline.triggers.BatteryPowerLevelTrigger;
import dji.sdk.mission.timeline.triggers.Trigger;
import dji.sdk.mission.timeline.triggers.TriggerEvent;
import dji.sdk.mission.timeline.triggers.WaypointReachedTrigger;

public class Timeline {
    private String timelineInfo;
    private TimelineEvent preEvent;
    private TimelineElement preElement;
    private DJIError preError;
    private String runningInfo;
    private static final int earthRadiusInMetres = 6371000;
    protected double newHomeLatitude;
    protected double newHomeLongitude;

    protected void stopTimeline() {
        MissionControl.getInstance().stopTimeline();
    }
    protected void pauseTimeline() {
        MissionControl.getInstance().pauseTimeline();
    }

    protected void resumeTimeline() {
        MissionControl.getInstance().resumeTimeline();
    }

    protected void setTimelinePlanToText(final String s) {
        timelineInfo = timelineInfo + s;
    }
    private Trigger.Listener triggerListener = new Trigger.Listener() {
        @Override
        public void onEvent(Trigger trigger, TriggerEvent event, @Nullable DJIError error) {
            setRunningResultToText("Trigger " + trigger.getClass().getSimpleName() + " event is " + event.name() + (error == null ? " " : error.getDescription()));
        }
    };
    private void initTrigger(final Trigger trigger) {
        trigger.addListener(triggerListener);
        trigger.setAction(new Trigger.Action() {
            @Override
            public void onCall() {
                setRunningResultToText("Trigger " + trigger.getClass().getSimpleName() + " Action method onCall() is invoked");
            }
        });
    }
    protected void addTrigger(Trigger trigger, Triggerable triggerTarget, String additionalComment) {

        if (triggerTarget != null) {

            initTrigger(trigger);
            List<Trigger> triggers = triggerTarget.getTriggers();
            if (triggers == null) {
                triggers = new ArrayList<>();
            }

            triggers.add(trigger);
            triggerTarget.setTriggers(triggers);

            setTimelinePlanToText(triggerTarget.getClass().getSimpleName()
                    + " Trigger "
                    + triggerTarget.getTriggers().size()
                    + ") "
                    + trigger.getClass().getSimpleName()
                    + additionalComment);
        }
    }

    protected void addBatteryPowerLevelTrigger(Triggerable triggerTarget) {
        float value = 20f;
        BatteryPowerLevelTrigger trigger = new BatteryPowerLevelTrigger();
        trigger.setPowerPercentageTriggerValue(value);
        addTrigger(trigger, triggerTarget, " at level " + value);
    }
    protected void addAircraftLandedTrigger(Triggerable triggerTarget) {
        AircraftLandedTrigger trigger = new AircraftLandedTrigger();
        addTrigger(trigger, triggerTarget, "");
    }
    protected void addWaypointReachedTrigger(Triggerable triggerTarget, int value) {
        WaypointReachedTrigger trigger = new WaypointReachedTrigger();
        trigger.setWaypointIndex(value);
        addTrigger(trigger, triggerTarget, " at index " + value);
    }
    protected void setRunningResultToText(final String s) {
        runningInfo = runningInfo + s;
    }

    protected void updateTimelineStatus(@Nullable TimelineElement element, TimelineEvent event, DJIError error) {

        if (element == preElement && event == preEvent && error == preError) {
            return;
        }

        if (element != null) {
            if (element instanceof TimelineMission) {
                setRunningResultToText(((TimelineMission) element).getMissionObject().getClass().getSimpleName()
                        + " event is "
                        + event.toString()
                        + " "
                        + (error == null ? "" : error.getDescription()));
            } else {
                setRunningResultToText(element.getClass().getSimpleName()
                        + " event is "
                        + event.toString()
                        + " "
                        + (error == null ? "" : error.getDescription()));
            }
        } else {
            setRunningResultToText("Timeline Event is " + event.toString() + " " + (error == null
                    ? ""
                    : "Failed:"
                    + error.getDescription()));
        }

        preEvent = event;
        preElement = element;
        preError = error;
    }

    protected void movePoint(double hLat, double hLon, double brng, double distanceInMetres) {
        double brngRad = Math.toRadians(brng);
        double latRad = Math.toRadians(hLat);
        double lonRad = Math.toRadians(hLon);
        double distFrac = distanceInMetres / earthRadiusInMetres;

        double latitudeResult = Math.asin(Math.sin(latRad) * Math.cos(distFrac) + Math.cos(latRad) * Math.sin(distFrac) * Math.cos(brngRad));
        double a = Math.atan2(Math.sin(brngRad) * Math.sin(distFrac) * Math.cos(latRad), Math.cos(distFrac) - Math.sin(latRad) * Math.sin(latitudeResult));
        double longitudeResult = (lonRad + a + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        newHomeLatitude = Math.toDegrees(latitudeResult);
        newHomeLongitude = Math.toDegrees(longitudeResult);
    }
}
