package com.roboticaircraftinspection.roboticinspection.utils;

import java.util.ArrayList;

public class GeoFenceUtil {
    public static double PI = 3.14159265;
    public static double TWOPI = 2*PI;
    /*public static void main(String[] args) {
        ArrayList<Double> lat_array = new ArrayList<Double>();
        ArrayList<Double> long_array = new ArrayList<Double>();

        ArrayList<String> polygon_lat_long_pairs = new ArrayList<String>();
        polygon_lat_long_pairs.add("31.000213,-87.584839");
        //lat/long of upper left tip of florida.
        polygon_lat_long_pairs.add("31.009629,-85.003052");
        polygon_lat_long_pairs.add("30.726726,-84.838257");
        polygon_lat_long_pairs.add("30.584962,-82.168579");
        polygon_lat_long_pairs.add("30.73617,-81.476441");
        //lat/long of upper right tip of florida.
        polygon_lat_long_pairs.add("29.002375,-80.795288");
        polygon_lat_long_pairs.add("26.896598,-79.938355");
        polygon_lat_long_pairs.add("25.813738,-80.059204");
        polygon_lat_long_pairs.add("24.93028,-80.454712");
        polygon_lat_long_pairs.add("24.401135,-81.817017");
        polygon_lat_long_pairs.add("24.700927,-81.959839");
        polygon_lat_long_pairs.add("24.950203,-81.124878");
        polygon_lat_long_pairs.add("26.0015,-82.014771");
        polygon_lat_long_pairs.add("27.833247,-83.014527");
        polygon_lat_long_pairs.add("28.8389,-82.871704");
        polygon_lat_long_pairs.add("29.987293,-84.091187");
        polygon_lat_long_pairs.add("29.539053,-85.134888");
        polygon_lat_long_pairs.add("30.272352,-86.47522");
        polygon_lat_long_pairs.add("30.281839,-87.628784");

        //Convert the strings to doubles.
        for(String s : polygon_lat_long_pairs){
            lat_array.add(Double.parseDouble(s.split(",")[0]));
            long_array.add(Double.parseDouble(s.split(",")[1]));
        }

        //prints TRUE true because the lat/long passed in is
        //inside the bounding box.
        System.out.println(coordinate_is_inside_polygon(
                25.7814014D,-80.186969D,
                lat_array, long_array));

        //prints FALSE because the lat/long passed in
        //is Not inside the bounding box.
        System.out.println(coordinate_is_inside_polygon(
                25.831538D,-1.069338D,
                lat_array, long_array));

    }*/
    public static boolean coordinate_is_inside_polygon(
            double latitude, double longitude,
            ArrayList<Double> lat_array, ArrayList<Double> long_array)
    {
        double angle=0;
        double point1_lat;
        double point1_long;
        double point2_lat;
        double point2_long;
        int latSize = lat_array.size();

        for (int i=0; i < latSize; i++) {
            point1_lat = lat_array.get(i) - latitude;
            point1_long = long_array.get(i) - longitude;
            point2_lat = lat_array.get((i+1)%latSize) - latitude;
            //
            point2_long = long_array.get((i+1)%latSize) - longitude;
            angle += Angle2D(point1_lat,point1_long,point2_lat,point2_long);
        }

        if (Math.abs(angle) < PI)
            return false;
        else
            return true;
    }

    public static double Angle2D(double y1, double x1, double y2, double x2)
    {
        double dtheta,theta1,theta2;

        theta1 = Math.atan2(y1,x1);
        theta2 = Math.atan2(y2,x2);
        dtheta = theta2 - theta1;
        while (dtheta > PI)
            dtheta -= TWOPI;
        while (dtheta < -PI)
            dtheta += TWOPI;

        return(dtheta);
    }

   /* private void setupCircularGeoFence() {
        // Setup a fence that has a radius of maximum dirension of the aircraft/2 + clearance... say 25 ft (Lets asme that this is R1)
        // This circle chould start from the mid point of the aircraft
        // Use Android Location class to check this against realTime data from drone.
        // As the drone keeps flying calculate the distance between the drones lat/long to the lat/long of the center point
        // of the drone. If it exceeds R1, isue a command to stop time line mission and return back to home point
        //
        //
        Location areaOfIinterest = new Location();
        Location currentPosition = new Location();

        areaOfIinterest.setLatitude(aoiLat);
        areaOfIinterest.setLongitude(aoiLong);

        currentPosition.setLatitude(myLat);
        currentPosition.setLongitude(myLong);

        float dist = areaOfIinterest.distanceTo(currentPosition);

        return (dist < 10000);
    }*/


}
