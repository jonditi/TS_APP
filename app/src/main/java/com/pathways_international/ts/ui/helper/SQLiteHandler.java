package com.pathways_international.ts.ui.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by android-dev on 5/16/17.
 */

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "top_secret";


    //  table one
    private static final String TABLE_ONE = "one";

    //  table two
    private static final String TABLE_TWO = "two";

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


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ONE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TWO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOC);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONSTITUENCIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WARDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POLL_STATIONS);
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


    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + TABLE_LOC;
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
