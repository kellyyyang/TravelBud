package com.codepath.travelbud.helper_classes;

import com.parse.ParseGeoPoint;

public class LocationTag {
    private ParseGeoPoint geoPoint;
    private String locationString;

    public LocationTag() {}

    public String getLocationString() {
        return locationString;
    }

    public ParseGeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setLocationString(String locationStringInput) {
        this.locationString = locationStringInput;
    }

    public void setGeoPoint(ParseGeoPoint geoPointInput) {
        this.geoPoint = geoPointInput;
    }
}

