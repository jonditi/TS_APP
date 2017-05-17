package com.pathways_international.ts.ui.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by android-dev on 5/17/17.
 */

public class LocationSharedPrefs {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public LocationSharedPrefs(Context activity) {
        sharedPreferences = activity.getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();

    }

    public String getConstituencyList() {
        return sharedPreferences.getString("constituency_list", "");
    }

    public void setConstituencyList(String countyList) {
        editor.putString("constituency_list", countyList);
        editor.apply();
    }

    public String getCountyList() {
        return sharedPreferences.getString("county_list", "");
    }

    public void setCountyList(String countyList) {
        editor.putString("county_list", countyList);
        editor.apply();
    }

    public String getCounty() {
        return sharedPreferences.getString("county", "");
    }

    public void setCounty(String county) {
        editor.putString("county", county);
        editor.apply();
    }

    public String getPlaceLabel() {
        return sharedPreferences.getString("place_label", "");
    }

    public void setPlaceLabel(String label) {
        editor.putString("place_label", label);
        editor.apply();
    }

    public void setPlaceLabelList(String labelList) {
        editor.putString("place_label_list", labelList);
        editor.apply();
    }


}

