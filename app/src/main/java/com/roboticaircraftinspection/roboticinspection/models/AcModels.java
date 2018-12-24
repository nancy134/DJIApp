package com.roboticaircraftinspection.roboticinspection.models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AcModels {
    private static Map<String, List<Integer>> acDim = new HashMap<String, List<Integer>>();

    public static final String[] ACMODELS = {
            "Select Aircraft","MD10-10","MD10-30","MD11","B757-200","B767-200", "B777", "A300-600", "A310-300", "OTHER"
    };

    public static Map<String, List<Integer>> setAcDimensions() {
        // We need a/c length, wing span , tail height, body height, mid point of wing from Nose, Wing angle - in feet because a/c drawings are in feet/inches
        // Array values are in this Order: length x width x tail height x distance to nose from starting point x wing position(mid point) from nose x body height x wing angle
        //[1 m = 3.3 ft]. Im retaining the same length and width, but adding 6 to 10 ft to nose and tail heights for safety
        acDim.put("Select Aircraft", Arrays.asList(new Integer[]{0, 0, 0, 0, 0, 0, 0}));
        acDim.put("MD10-10", Arrays.asList(new Integer[]{182, 156, 70, 10, 100, 40, 60})); // Actual dims: 182ft x 156ft x 59ft x 10 ft. x 100 x xx deg
        acDim.put("MD10-30", Arrays.asList(new Integer[]{182, 156, 70, 10, 100, 40, 60})); // Actual dims: 182ft x 156ft x 59ft x 10 ft. x 100 x xx deg
        acDim.put("MD11", Arrays.asList(new Integer[]{203, 171, 70, 10, 90, 40, 60}));// Actual dims: 203ft x 171ft x 58ft x 10 ft. x 90 x 26 x 60 deg
        acDim.put("B757-200", Arrays.asList(new Integer[]{156, 125, 55, 10, 75, 30, 65})); // Actual dims: 156 ft x 125ft x 45ft x 10ft x 75 x 21 x 25 deg[sweep angle]
        acDim.put("B767-200", Arrays.asList(new Integer[]{160, 156, 60, 10, 90, 30, 75}));
        acDim.put("B777", Arrays.asList(new Integer[]{210, 220, 70, 10, 105, 40, 75}));
        acDim.put("A300-600", Arrays.asList(new Integer[]{180, 150, 60, 10, 85, 30, 75}));
        acDim.put("A310-300", Arrays.asList(new Integer[]{160, 150, 60, 10, 75, 30, 75}));

        return acDim;
    }

    public static void setOtherDimensions(String acModel, int xVal, int yVal, int zVal, int oVal, int wpValue, int bhValue, int wAngle)
    {
        acDim.put(acModel, Arrays.asList(new Integer[]{xVal, yVal, zVal, oVal, wpValue, bhValue, wAngle}));
    }

    public static double getLengthInFt(String acModel)
    {
        return (double) acDim.get(acModel).get(0);
    }

    public static double getWidthInFt(String acModel)
    {
        return (double) acDim.get(acModel).get(1);
    }

    public static double getBodyHtInFt(String acModel)
    {
        return (double)acDim.get(acModel).get(5);
    }

    public static double getTailHtInFt(String acModel)
    {
        return (double) acDim.get(acModel).get(2);
    }

    public static double getLengthInMeters(String acModel)
    {
        return (double) acDim.get(acModel).get(0) / 3.3;
    }

    public static double getWidthInMeters(String acModel)
    {
        return (double) acDim.get(acModel).get(1) / 3.3;
    }

    public static double getTailHtInMeters(String acModel)
    {
        return (double) acDim.get(acModel).get(2) / 3.3;
    }

    public static double getStartDistInMeters(String acModel)
    {
        return (double)acDim.get(acModel).get(3) / 3.3;
    }

    public static double getWingPosInMeters(String acModel)
    {
        return (double) acDim.get(acModel).get(4) / 3.3;
    }

    public static double getBodyHtInMeters(String acModel)
    {
        return (double)acDim.get(acModel).get(5) / 3.3;
    }

    public static double getAngleTravelInMeters(String acModel)
    {
        return (getWidthInMeters(acModel) / 2) / Math.cos((Math.toRadians(90 - acDim.get(acModel).get(6))));
    }

    public static double getAngleTravelOffsetInMeters(String acModel)
    {
        return (double)getAngleTravelInMeters(acModel) * Math.cos((Math.toRadians(acDim.get(acModel).get(6))));
    }

    public static int getWingAngle(String acModel)
    {
        return acDim.get(acModel).get(6);
    }

}
