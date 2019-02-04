package com.roboticaircraftinspection.roboticinspection.utils;

import com.roboticaircraftinspection.roboticinspection.models.AcModels;

import java.util.ArrayList;

import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionGotoWaypointMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;

/**
 * Created by dji on 15/12/18.
 */

public class GeneralUtils {
    public static final double ONE_METER_OFFSET = 0.00000899322;
    private static long lastClickTime;
    private static final int EARTH_RADIUS_METERS = 6371000;


    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 800) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static boolean checkGpsCoordinate(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f
                && longitude != 0f);
    }

    public static double toRadian(double x) {
        return x * Math.PI / 180.0;
    }

    public static double toDegree(double x) {
        return x * 180 / Math.PI;
    }

    public static double cosForDegree(double degree) {
        return Math.cos(degree * Math.PI / 180.0f);
    }

    public static double sinForDegree(double degree) {
        return Math.sin(degree * Math.PI / 180.0f);
    }

    public static double calcLongitudeOffset(double latitude) {
        return ONE_METER_OFFSET / cosForDegree(latitude);
    }

    public static void addLineToSB(StringBuffer sb, String name, Object value) {
        if (sb == null) return;
        sb.
                append(name == null ? "" : name + ": ").
                append(value == null ? "" : value + "").
                append("\n");
    }

    /**
     * calculates the azimuth in degrees from start point to end point");
     double startLat = Math.toRadians(start.lat);
     * @param-start
     * @param-end
     * @return
     */
    public static double calculateBearingUsingLatLong(double latOne, double longOne, double latTwo, double longTwo)
    {
        double phi1 = latOne * Math.PI / 180.0;
        double phi2 = latTwo * Math.PI / 180.0;
        double lam1 = longOne * Math.PI / 180.0;
        double lam2 = longTwo * Math.PI / 180.0;

        double azimuthDeg = (Math.toDegrees(Math.atan2(Math.sin(lam2-lam1)*Math.cos(phi2),
                Math.cos(phi1) * Math.sin(phi2) - Math.sin(phi1) * Math.cos(phi2) * Math.cos(lam2-lam1))) + 360)%360;

        if (azimuthDeg < 0)
        {
            azimuthDeg += 360;
//            azimuthDeg = (azimuthDeg * -1) - 180;
        }
        if (azimuthDeg > 180) {
//            azimuthDeg = 180 - azimuthDeg;// - 360;
            azimuthDeg = azimuthDeg - 360;
        }
        //
        /*if (droneRotationAngle < -180) {
            droneRotationAngle = droneRotationAngle + 180;
        }else  if (droneRotationAngle > 180) {
            droneRotationAngle = droneRotationAngle - 360;
        }*/
        return azimuthDeg;
    }

    /**
     * returns every coordinate pair in between two coordinate pairs given the desired interval
     * @return
     */
    public static ArrayList<GeoLocation> getLocations(int nbrWayPts, double azimuth, double latOne, double longOne, double latTwo, double longTwo) {
        GeoLocation start =  new GeoLocation(latOne, longOne);
        GeoLocation end =  new GeoLocation(latTwo, longTwo);
        //
        double d = getPathLength(start, end);
        double dist =  d / nbrWayPts;
        double coveredDist = dist;
        ArrayList<GeoLocation> coords = new ArrayList<GeoLocation>();
//        coords.add(new GeoLocation(start.lat, start.lng));
        for(int distance = 0; distance < nbrWayPts - 1; distance++){// += interval) {
            GeoLocation coord = getDestinationLatLng(start.lat, start.lng, azimuth, coveredDist);
            coveredDist += dist;
            coords.add(coord);
        }
//        coords.add(new GeoLocation(end.lat, end.lng));

        return coords;

    }

    /**
     * calculates the distance between two lat, long coordinate pairs
     * @return
     */
    public static double getPathLength(double sLat, double sLng, double eLat, double eLng) {
        return getPathLength(new GeoLocation(sLat,sLng), new GeoLocation(eLat,eLng));
    }
    /**
     * calculates the distance between two lat, long coordinate pairs
     * @param start
     * @param end
     * @return
     */
    public static double getPathLength(GeoLocation start, GeoLocation end) {
        double lat1Rads = Math.toRadians(start.lat);
        double lat2Rads = Math.toRadians(end.lat);
        double deltaLat = Math.toRadians(end.lat - start.lat);

        double deltaLng = Math.toRadians(end.lng - start.lng);
        double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) + Math.cos(lat1Rads) * Math.cos(lat2Rads) * Math.sin(deltaLng/2) * Math.sin(deltaLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = EARTH_RADIUS_METERS * c;
        return d;
    }

    /**
     * returns the lat an long of destination point given the start lat, long, aziuth, and distance
     * @param lat
     * @param lng
     * @param azimuth
     * @param distance
     * @return
     */
    private static GeoLocation getDestinationLatLng(double lat, double lng, double azimuth, double distance) {
        double radiusKm = EARTH_RADIUS_METERS / 1000; //Radius of the Earth in km
        double brng = Math.toRadians(azimuth); //Bearing is degrees converted to radians.
        double d = distance / 1000; //Distance m converted to km
        double lat1 = Math.toRadians(lat); //Current dd lat point converted to radians
        double lon1 = Math.toRadians(lng); //Current dd long point converted to radians
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d / radiusKm) + Math.cos(lat1) * Math.sin(d / radiusKm) * Math.cos(brng));
        double lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(d / radiusKm) * Math.cos(lat1), Math.cos(d / radiusKm) - Math.sin(lat1) * Math.sin(lat2));
        //convert back to degrees
        lat2 = Math.toDegrees(lat2);
        lon2 = Math.toDegrees(lon2);
        return new GeoLocation(lat2, lon2);
    }

    public static WaypointMission.Builder getWaypointMissionBuilder()
    {
        WaypointMission.Builder waypointMissionBuilder = new WaypointMission.Builder().autoFlightSpeed(2f)
                .maxFlightSpeed(5f)
                .setExitMissionOnRCSignalLostEnabled(false)
                .finishedAction(
                        WaypointMissionFinishedAction.NO_ACTION)
                .flightPathMode(
                        WaypointMissionFlightPathMode.NORMAL)
                .gotoFirstWaypointMode(
                        WaypointMissionGotoWaypointMode.SAFELY)
                .headingMode(
                        WaypointMissionHeadingMode.AUTO)
                .repeatTimes(1);
        return waypointMissionBuilder;
    }

    private static GeoLocation movePoint(double hLat, double hLon, double brng, double distanceInMetres) {
        double brngRad = Math.toRadians(brng);
        double latRad = Math.toRadians(hLat);
        double lonRad = Math.toRadians(hLon);
        double distFrac = distanceInMetres / EARTH_RADIUS_METERS;

        double latitudeResult = Math.asin(Math.sin(latRad) * Math.cos(distFrac) + Math.cos(latRad) * Math.sin(distFrac) * Math.cos(brngRad));
        double a = Math.atan2(Math.sin(brngRad) * Math.sin(distFrac) * Math.cos(latRad), Math.cos(distFrac) - Math.sin(latRad) * Math.sin(latitudeResult));
        double longitudeResult = (lonRad + a + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        return new GeoLocation(Math.toDegrees(latitudeResult),Math.toDegrees(longitudeResult));

    }

    public static ArrayList<GeoLocation> buildGeoFencePolygon(double homeLatitude, double homeLongitude, double droneOrientation, String acModel) {
        ArrayList<GeoLocation> geoArray = new ArrayList<GeoLocation>();
        geoArray. add(movePoint(homeLatitude, homeLongitude, droneOrientation + 90, -(AcModels.getWidthInMeters(acModel) / 2 + 3 ))); // Add 10 ft or 3 meters
        geoArray. add(movePoint(homeLatitude, homeLongitude, droneOrientation, AcModels.getLengthInMeters(acModel) + 6)); // 3 meter safety + 10 ft starting point
        geoArray. add(movePoint(homeLatitude, homeLongitude, droneOrientation + 90, (AcModels.getWidthInMeters(acModel) + 6)));
        geoArray. add(movePoint(homeLatitude, homeLongitude, droneOrientation, -(AcModels.getLengthInMeters(acModel) + 6)));
        return geoArray;
    }

    public static ArrayList<GeoLocation> buildGeoFencePolygonTest(double homeLatitude, double homeLongitude, double droneOrientation, String acModel) {
        ArrayList<GeoLocation> geoArray = new ArrayList<GeoLocation>();
        geoArray. add(movePoint(homeLatitude, homeLongitude, droneOrientation + 90, -(10 / 2 + 3))); // Add 10 ft or 3 meters
        geoArray. add(movePoint(homeLatitude, homeLongitude, droneOrientation, 10 + 6)); // 3 meter safety + 10 ft starting point
        geoArray. add(movePoint(homeLatitude, homeLongitude, droneOrientation + 90, (10 + 6)));
        geoArray. add(movePoint(homeLatitude, homeLongitude, droneOrientation, -(10 + 6)));
        return geoArray;
    }
    public static double getRadius(double x, double y){
        return Math.sqrt((x*x) + (y*y));
    }
    public static double getX(double x, double y, double heading){
        return getRadius(x, y) * (Math.cos(Math.toRadians(heading)));
    }
    public static double getY(double x, double y, double heading){
        return getRadius(x, y) * (Math.sin(Math.toRadians(heading)));
    }
}
