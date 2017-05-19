package com.pathways_international.ts.ui.utils;

/**
 * Created by android-dev on 5/19/17.
 */

public class Urls {
    // Default config for real devices
    private static String baseUrl =
            // ip of your machine
            "http://inovatec.co.ke/redwood/";
    // Server patient login url
    public static String CONSTITUENCIES = baseUrl + "constituency.php?county=";
    public static String WARDS = baseUrl + "ward.php?constituency=";
    public static String POLL_STATION = baseUrl + "poll_station.php?ward=";
}
