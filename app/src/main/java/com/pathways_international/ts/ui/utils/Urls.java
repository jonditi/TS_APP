package com.pathways_international.ts.ui.utils;

/**
 * Created by android-dev on 5/19/17.
 */

public class Urls {
    // Default config for real devices
    private static String baseUrl =
            // ip of your machine
            "http://inovatec.co.ke/ts/";
    // Server patient login url
    public static String CONSTITUENCIES = baseUrl + "constituency.php?county=";
    public static String WARDS = baseUrl + "ward.php?constituency=";
    public static String POLL_STATION = baseUrl + "poll_station.php?ward=";
    public static String PUSH_TO_TABELE_ONE = baseUrl + "post_tabele_one.php";
    public static String PUSH_TO_TABLE_TWO = baseUrl + "post_table_two.php";
}
