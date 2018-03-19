package com.pathways_international.ts.ui.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by android-dev on 3/16/18.
 */

public class VoterTurnout {
    @SerializedName("id")
    private String mId;

    @SerializedName("poll_station_id")
    private String pollStationId;

    @SerializedName("voters")
    private String totalString;

    @SerializedName("time_on_device")
    private String timeOnDevice;

    public VoterTurnout() {
    }

    public VoterTurnout(String pollStationId, String totalString, String timeOnDevice) {
        this.pollStationId = pollStationId;
        this.totalString = totalString;
        this.timeOnDevice = timeOnDevice;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getPollStationId() {
        return pollStationId;
    }

    public void setPollStationId(String pollStationId) {
        this.pollStationId = pollStationId;
    }

    public String getTimeOnDevice() {
        return timeOnDevice;
    }

    public void setTimeOnDevice(String timeOnDevice) {
        this.timeOnDevice = timeOnDevice;
    }

    public String getTotalString() {
        return totalString;
    }

    public void setTotalString(String totalString) {
        this.totalString = totalString;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VoterTurnout && ((VoterTurnout) obj).mId.equals(mId);
    }
}
