package com.pathways_international.ts.ui.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by android-dev on 3/15/18.
 */

public class Uploads {
    @SerializedName("id")
    private String mId;

    @SerializedName("image")
    private String mImage;

    @SerializedName("poll_station_id")
    private String mPollStationId;

    public Uploads() {
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getmImage() {
        return mImage;
    }

    public void setmImage(String mImage) {
        this.mImage = mImage;
    }

    public String getmPollStationId() {
        return mPollStationId;
    }

    public void setmPollStationId(String mPollStationId) {
        this.mPollStationId = mPollStationId;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Uploads && ((Uploads) obj).mId.equals(mId);
    }
}
