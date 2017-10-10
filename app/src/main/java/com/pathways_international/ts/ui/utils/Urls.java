package com.pathways_international.ts.ui.utils;

/**
 * Created by android-dev on 5/19/17.
 */

public class Urls {
    // Default config for real devices
    private static String baseUrl =
            // ip of your machine
            "https://fundilistapp.com/ts/";

    private static String localBaseUrl = "http://192.168.100.7/ts-login/";
    // Server patient login url
    public static String CONSTITUENCIES = baseUrl + "const_name.php?county=";
    public static String WARDS = baseUrl + "ward_name.php?constituency=";
    public static String POLL_STATION = baseUrl + "pollstation_name.php?ward=";
    public static String POLL_STREAM = baseUrl + "pollstation_stream.php?pname=";
    public static String POLL_STREAM_TURNOUT = baseUrl + "stream_turnout.php?pname=";
    public static String PUSH_TO_TABELE_ONE = baseUrl + "post_tabele_one.php";
    public static String PUSH_TO_TABLE_TWO = baseUrl + "post_table_two.php";
    public static String PUSH_TO_TABLE_TWO_DEV = baseUrl + "push_table_two_dev.php";
    public static String UPLOAD_IMAGE = baseUrl + "upload_image.php";
    public static String UPLOAD_TURNOUT_IMAGE = baseUrl + "upload_turnout_image.php";
    public static String LOGIN = baseUrl + "login.php";
    public static String REGISTER = baseUrl + "register.php";
    public static String PUSH_TO_VOTER_TURNOUT = baseUrl + "push_to_voter_turnout.php";
    public static String VERIFY_KEY = baseUrl + "verify_key.php";
}
