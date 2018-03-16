package com.pathways_international.ts.ui.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by android-dev on 3/16/18.
 */

public class ConstituencyUploads {
    @SerializedName("id")
    private String mId;

    @SerializedName("image")
    private String image;

    @SerializedName("constituency_code")
    private String constituencyCode;

    public ConstituencyUploads() {
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getConstituencyCode() {
        return constituencyCode;
    }

    public void setConstituencyCode(String constituencyCode) {
        this.constituencyCode = constituencyCode;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ConstituencyUploads && ((ConstituencyUploads) obj).mId.equals(mId);
    }
}
