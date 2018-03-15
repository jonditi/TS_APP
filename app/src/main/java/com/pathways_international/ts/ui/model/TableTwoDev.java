package com.pathways_international.ts.ui.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by android-dev on 3/12/18.
 */

public class TableTwoDev {

    @SerializedName("id")
    private String mId;

    @SerializedName("poll_station_id")
    private String pollStationId;

    @SerializedName("raila")
    private String raila;

    @SerializedName("uhuru")
    private String uhuru;

    @SerializedName("registered_voters")
    private String registeredVoters;

    @SerializedName("rejected_ballot_papers")
    private String rejectedBallotPapers;

    @SerializedName("rejected_objected")
    private String rejectedObjected;

    @SerializedName("disputed_votes")
    private String disputedVotes;

    @SerializedName("valid_votes_cast")
    private String validVotesCast;

    @SerializedName("time_on_device")
    private String timeOnDevice;

    public TableTwoDev() {
    }

    public TableTwoDev(String pollStationId, String raila, String uhuru, String registeredVoters,
                       String rejectedBallotPapers, String rejectedObjected, String disputedVotes,
                       String validVotesCast, String timeOnDevice) {
        this.pollStationId = pollStationId;
        this.raila = raila;
        this.uhuru = uhuru;
        this.registeredVoters = registeredVoters;
        this.rejectedBallotPapers = rejectedBallotPapers;
        this.rejectedObjected = rejectedObjected;
        this.disputedVotes = disputedVotes;
        this.validVotesCast = validVotesCast;
        this.timeOnDevice = timeOnDevice;
    }

    public String getPollStationId() {
        return pollStationId;
    }

    public void setPollStationId(String pollStationId) {
        this.pollStationId = pollStationId;
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

    public String getRejectedBallotPapers() {
        return rejectedBallotPapers;
    }

    public void setRejectedBallotPapers(String rejectedBallotPapers) {
        this.rejectedBallotPapers = rejectedBallotPapers;
    }

    public String getRejectedObjected() {
        return rejectedObjected;
    }

    public void setRejectedObjected(String rejectedObjected) {
        this.rejectedObjected = rejectedObjected;
    }

    public String getDisputedVotes() {
        return disputedVotes;
    }

    public void setDisputedVotes(String disputedVotes) {
        this.disputedVotes = disputedVotes;
    }

    public String getValidVotesCast() {
        return validVotesCast;
    }

    public void setValidVotesCast(String validVotesCast) {
        this.validVotesCast = validVotesCast;
    }

    public String getTimeOnDevice() {
        return timeOnDevice;
    }

    public void setTimeOnDevice(String timeOnDevice) {
        this.timeOnDevice = timeOnDevice;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TableTwoDev && ((TableTwoDev) obj).mId.equals(mId);
    }
}
