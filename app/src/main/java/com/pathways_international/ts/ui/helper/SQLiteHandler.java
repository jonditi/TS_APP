package com.pathways_international.ts.ui.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by android-dev on 5/16/17.
 */

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 5;

    // Database Name
    private static final String DATABASE_NAME = "top_secret";


    //  table one
    private static final String TABLE_ONE = "one";

    //  table two
    private static final String TABLE_TWO = "two";

    // Login table name
    private static final String TABLE_USER = "user";

    // loc table;
    private static final String TABLE_LOC = "location_all";
    private static final String TABLE_CONSTITUENCIES = "constituencies";
    private static final String TABLE_WARDS = "wards";
    private static final String TABLE_POLL_STATIONS = "poll_stations";

    // Column names
    private static final String KEY_ID = "id";
    private static final String KEY_COUNTY = "county";
    private static final String KEY_CONSTITUENCY = "constituency";
    private static final String KEY_WARD = "ward";
    private static final String KEY_POLL_STATION = "poll_station";
    private static final String KEY_POLL_STATION_ID = "poll_station_id";
    private static final String KEY_NUM_ONE = "num_one";
    private static final String KEY_NUM_TWO = "num_two";
    private static final String KEY_SEAT = "seat";

    // Login Table Columns names
    private static final String KEY_FIRST_NAME = "first_name";
    private static final String KEY_LAST_NAME = "last_name";
    private static final String KEY_ID_NUMBER = "id_number";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "uid";
    private static final String KEY_CONST_CODE = "constituency_code";
    private static final String KEY_CONST_NAME = "constituency_name";
    private static final String KEY_WARD_NAME = "ward_name";
    private static final String KEY_WARD_CODE = "ward_code";
    private static final String KEY_CREATED_AT = "created_at";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE_ONE = "CREATE TABLE IF NOT EXISTS " + TABLE_ONE + "("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_COUNTY + " TEXT,"
                + KEY_CONSTITUENCY + " TEXT," + KEY_WARD + " TEXT,"
                + KEY_POLL_STATION + " TEXT" + ")";
        db.execSQL(CREATE_TABLE_ONE);

        String CREATE_TABLE_CONSTITUENCIES = "CREATE TABLE IF NOT EXISTS " + TABLE_CONSTITUENCIES + "("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_CONSTITUENCY + " TEXT" + ")";
        db.execSQL(CREATE_TABLE_CONSTITUENCIES);


        String CREATE_TABLE_WARDS = "CREATE TABLE IF NOT EXISTS " + TABLE_WARDS + "("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_WARD + " TEXT" + ")";
        db.execSQL(CREATE_TABLE_WARDS);


        String CREATE_TABLE_POLL_STATIONS = "CREATE TABLE IF NOT EXISTS " + TABLE_POLL_STATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_POLL_STATION + " TEXT,"
                + KEY_POLL_STATION_ID + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE_POLL_STATIONS);

        String CREATE_TABLE_TWO = "CREATE TABLE IF NOT EXISTS " + TABLE_TWO + "("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_POLL_STATION_ID + " TEXT,"
                + KEY_NUM_ONE + " TEXT," + KEY_NUM_TWO + " TEXT,"
                + KEY_SEAT + " TEXT" + ")";
        db.execSQL(CREATE_TABLE_TWO);

        String CREATE_TABLE_LOC = "CREATE TABLE IF NOT EXISTS " + TABLE_LOC + "("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_COUNTY + " TEXT,"
                + KEY_CONSTITUENCY + " TEXT," + KEY_WARD + " TEXT,"
                + KEY_POLL_STATION_ID + " TEXT,"
                + KEY_POLL_STATION + " TEXT" + ")";
        db.execSQL(CREATE_TABLE_LOC);

        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_FIRST_NAME + " TEXT,"
                + KEY_LAST_NAME + " TEXT," + KEY_ID_NUMBER + " TEXT UNIQUE,"
                + KEY_PHONE + " TEXT,"
                + KEY_UID + " TEXT," + KEY_CONST_CODE + " TEXT, " + KEY_CONST_NAME + " TEXT,"
                + KEY_COUNTY + " TEXT,"
                + KEY_WARD_NAME + " TEXT," + KEY_WARD_CODE + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ONE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TWO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONSTITUENCIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WARDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POLL_STATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        onCreate(db);
    }

    /**
     * Storing user details in database
     */
    public void addUser(String firstName, String lastName, String idNumber, String phone, String uid, String constCode,
                        String constName, String countyName, String wardName, String wardCode, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FIRST_NAME, firstName); // first name
        values.put(KEY_LAST_NAME, lastName); // last name
        values.put(KEY_ID_NUMBER, idNumber); // id
        values.put(KEY_PHONE, phone); // phone
        values.put(KEY_UID, uid); // uid
        values.put(KEY_CONST_CODE, constCode); // Constituency code
        values.put(KEY_CONST_NAME, constName); // Constituency name
        values.put(KEY_COUNTY, countyName); // County name
        values.put(KEY_WARD_NAME, wardName); // ward name
        values.put(KEY_WARD_CODE, wardCode); // ward code
        values.put(KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    /**
     * Re crate database Delete all tables and create them again
     */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }

    public void addToTableOne(String county, String constituency, String ward, String pollStation) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_COUNTY, county);
        values.put(KEY_CONSTITUENCY, constituency);
        values.put(KEY_WARD, ward);
        values.put(KEY_POLL_STATION, pollStation);

        long id = db.insert(TABLE_ONE, null, values);
        Log.d(TAG, "Data inserted in table one:" + id);
    }

    public void addToTableTwo(String pollStationId, String numOne, String numTwo, String seat) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_POLL_STATION_ID, pollStationId);
        values.put(KEY_NUM_ONE, numOne);
        values.put(KEY_NUM_TWO, numTwo);
        values.put(KEY_SEAT, seat);

        long id = db.insert(TABLE_TWO, null, values);

        Log.d(TAG, "Data inserted in table two:" + id);
    }

    public void addToLoc(String county, String pollStId, String constituency, String ward, String pollStation) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_COUNTY, county);
        values.put(KEY_POLL_STATION_ID, pollStId);
        values.put(KEY_CONSTITUENCY, constituency);
        values.put(KEY_WARD, ward);
        values.put(KEY_POLL_STATION, pollStation);

        long id = db.insert(TABLE_LOC, null, values);
        Log.d(TAG, "Data inserted in table one:" + id);
    }

    public void insertIntoConst(String constituency) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CONSTITUENCY, constituency);

        long id = db.insert(TABLE_CONSTITUENCIES, null, values);
        Log.d(TAG, "Constituency inserted:" + id);
    }

    public void deleteConsti() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONSTITUENCIES, null, null);
        db.close();
    }

    public void insertIntoWard(String ward) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_WARD, ward);

        long id = db.insert(TABLE_WARDS, null, values);
        Log.d(TAG, "ward inserted:" + id);
    }

    public void deleteWards() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WARDS, null, null);
        db.close();
    }

    public void insertIntoPollStations(String pollStation, String pollStationId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_POLL_STATION, pollStation);
        values.put(KEY_POLL_STATION_ID, pollStationId);

        long id = db.insert(TABLE_CONSTITUENCIES, null, values);
        Log.d(TAG, "PollStation inserted:" + id);
    }

    public void deletePollSt() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_POLL_STATIONS, null, null);
        db.close();
    }

    /**
     * Getting user data from database
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> client = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER + " WHERE id =" + KEY_ID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            // Column indices
            int firstName = cursor.getColumnIndex("first_name");
            int lastName = cursor.getColumnIndex("last_name");
            int idNumber = cursor.getColumnIndex("id_number");
            int phone = cursor.getColumnIndex("phone");
            int constCode = cursor.getColumnIndex("constituency_code");
            int constName = cursor.getColumnIndex("constituency_name");
            int countyName = cursor.getColumnIndex("county");
            int wardName = cursor.getColumnIndex("ward_name");
            int wardCode = cursor.getColumnIndex("ward_code");
            int uid = cursor.getColumnIndex("uid");

            client.put("first_name", cursor.getString(firstName));
            client.put("last_name", cursor.getString(lastName));
            client.put("id_number", cursor.getString(idNumber));
            client.put("phone", cursor.getString(phone));
            client.put("constituency_code", cursor.getString(constCode));
            client.put("uid", cursor.getString(uid));
            client.put("constituency_name", cursor.getString(constName));
            client.put("county_name", cursor.getString(countyName));
            client.put("ward_name", cursor.getString(wardName));
            client.put("ward_code", cursor.getString(wardCode));
        }
        cursor.close();
        db.close();
        // return client
        Log.d(TAG, "Fetching client from Sqlite: " + client.toString());

        return client;
    }


    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + TABLE_USER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        // return row count
        return rowCount;
    }

    public ArrayList<String> getCounties() {
        ArrayList<String> countyList = new ArrayList<>();
        String sql = "SELECT DISTINCT county FROM " + TABLE_LOC + " ORDER BY county ASC";

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int countyIndex = cursor.getColumnIndex("county");
                String county = cursor.getString(countyIndex);
                countyList.add(county);
//
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();

        return countyList;
    }

    public ArrayList<String> getConstituencies(String county) {
        ArrayList<String> countyList = new ArrayList<>();
        String sql = "SELECT constituency FROM " + TABLE_LOC + " WHERE county = " + "'" + county + "'" + " ORDER BY constituency ASC";

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int constituencyIndex = cursor.getColumnIndex("constituency");
                String constituency = cursor.getString(constituencyIndex);
                countyList.add(constituency);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();

        return countyList;
    }

    public ArrayList<String> getWards(String constituency) {
        ArrayList<String> countyList = new ArrayList<>();
        String sql = "SELECT ward FROM " + TABLE_LOC + " WHERE constituency = " + "'" + constituency + "'" + " ORDER BY ward ASC";

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int wardIndex = cursor.getColumnIndex("ward");
                String ward = cursor.getString(wardIndex);
                countyList.add(ward);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();

        return countyList;
    }

    public ArrayList<String> getPollStations(String ward) {
        ArrayList<String> countyList = new ArrayList<>();
        String sql = "SELECT poll_station FROM " + TABLE_LOC + " WHERE ward = " + "'" + ward + "'" + " ORDER BY poll_station ASC";

        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                int pollStIndex = cursor.getColumnIndex("poll_station");
                String pollStation = cursor.getString(pollStIndex);
                countyList.add(pollStation);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();

        return countyList;
    }





}
