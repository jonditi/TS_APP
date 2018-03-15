package com.pathways_international.ts.ui.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by android-dev on 3/12/18.
 */

public class TableOne {

    @com.google.gson.annotations.SerializedName("id")
    public String mId;

    @com.google.gson.annotations.SerializedName("poll_center")
    private String pollCenter;

    @SerializedName("ward_name")
    private String wardName;

    @SerializedName("constituency_name")
    private String constituencyName;

    @SerializedName("county_name")
    private String countyName;

    @SerializedName("stream")
    private String stream;


    public TableOne() {
    }


    public TableOne(String pollCenter, String wardName, String constituencyName, String countyName, String stream) {
        this.pollCenter = pollCenter;
        this.wardName = wardName;
        this.constituencyName = constituencyName;
        this.countyName = countyName;
        this.stream = stream;
    }

    public String getPollCenter() {
        return pollCenter;
    }

    public void setPollCenter(String pollCenter) {
        this.pollCenter = pollCenter;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public String getConstituencyName() {
        return constituencyName;
    }

    public void setConstituencyName(String constituencyName) {
        this.constituencyName = constituencyName;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TableOne && ((TableOne) obj).mId.equals(mId);
    }
}
