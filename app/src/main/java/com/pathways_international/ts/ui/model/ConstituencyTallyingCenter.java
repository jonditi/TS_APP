package com.pathways_international.ts.ui.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by android-dev on 3/16/18.
 */

public class ConstituencyTallyingCenter {
    @SerializedName("id")
    private String mId;

    @SerializedName("polling_station_code")
    private String pollingStationCode;

    @SerializedName("polling_station_name")
    private String pollingStationName;

    @SerializedName("raila")
    private String raila;

    @SerializedName("uhuru")
    private String uhuru;

    @SerializedName("registered_voters")
    private String registeredVoters;

    @SerializedName("rejected_ballot")
    private String rejectedBallot;

    @SerializedName("valid_votes_cast")
    private String validVotesCast;

    @SerializedName("county")
    private String county;

    @SerializedName("constituency")
    private String constituency;

    public ConstituencyTallyingCenter() {
    }

    public ConstituencyTallyingCenter(String pollingStationCode, String pollingStationName,
                                      String raila, String uhuru, String registeredVoters, String rejectedBallot,
                                      String validVotesCast, String county, String constituency) {
        this.pollingStationCode = pollingStationCode;
        this.pollingStationName = pollingStationName;
        this.raila = raila;
        this.uhuru = uhuru;
        this.registeredVoters = registeredVoters;
        this.rejectedBallot = rejectedBallot;
        this.validVotesCast = validVotesCast;
        this.county = county;
        this.constituency = constituency;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getPollingStationCode() {
        return pollingStationCode;
    }

    public void setPollingStationCode(String pollingStationCode) {
        this.pollingStationCode = pollingStationCode;
    }

    public String getPollingStationName() {
        return pollingStationName;
    }

    public void setPollingStationName(String pollingStationName) {
        this.pollingStationName = pollingStationName;
    }

    public String getRaila() {
        return raila;
    }

    public void setRaila(String raila) {
        this.raila = raila;
    }

    public String getUhuru() {
        return uhuru;
    }

    public void setUhuru(String uhuru) {
        this.uhuru = uhuru;
    }

    public String getRegisteredVoters() {
        return registeredVoters;
    }

    public void setRegisteredVoters(String registeredVoters) {
        this.registeredVoters = registeredVoters;
    }

    public String getRejectedBallot() {
        return rejectedBallot;
    }

    public void setRejectedBallot(String rejectedBallot) {
        this.rejectedBallot = rejectedBallot;
    }

    public String getValidVotesCast() {
        return validVotesCast;
    }

    public void setValidVotesCast(String validVotesCast) {
        this.validVotesCast = validVotesCast;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getConstituency() {
        return constituency;
    }

    public void setConstituency(String constituency) {
        this.constituency = constituency;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ConstituencyTallyingCenter && ((ConstituencyTallyingCenter) obj).mId.equals(mId);
    }
}
