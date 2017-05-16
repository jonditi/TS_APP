package com.pathways_international.ts.ui.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by android-dev on 5/16/17.
 */

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "top_secret";


    //  table one
    private static final String TABLE_ONE = "one";

    //  table two
    private static final String TABLE_TWO = "two";

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

        String CREATE_TABLE_TWO = "CREATE TABLE IF NOT EXISTS " + TABLE_TWO + "("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_POLL_STATION_ID + " TEXT,"
                + KEY_NUM_ONE + " TEXT," + KEY_NUM_TWO + " TEXT,"
                + KEY_SEAT + " TEXT" + ")";
        db.execSQL(CREATE_TABLE_TWO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ONE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TWO);
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
}
