package com.roboticaircraftinspection.roboticinspection.rest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface Api {
    String BASE_URL = "http://dev.virtualopenexchange.com/vex_pages/";

    @GET("aircraft.php")
    Call<List<AircraftRemote>> getAircraft();

    @GET("waypoints.php")
    Call<List<WaypointsRemote>> getWaypoints();
}
