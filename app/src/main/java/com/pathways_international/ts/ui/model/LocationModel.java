package com.pathways_international.ts.ui.model;

/**
 * Created by android-dev on 5/15/17.
 */

public class LocationModel {
    private String updated_at;

    private String county_label;

    private String county_id;

    private String location_label;

    private String created_at;

    private String longitude;

    private String latitude;

    private String location_id;

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getCounty_label() {
        return county_label;
    }

    public void setCounty_label(String county_label) {
        this.county_label = county_label;
    }

    public String getCounty_id() {
        return county_id;
    }

    public void setCounty_id(String county_id) {
        this.county_id = county_id;
    }

    public String getLocation_label() {
        return location_label;
    }

    public void setLocation_label(String location_label) {
        this.location_label = location_label;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLocation_id() {
        return location_id;
    }

    public void setLocation_id(String location_id) {
        this.location_id = location_id;
    }

}
