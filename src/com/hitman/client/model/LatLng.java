package com.hitman.client.model;

import android.location.Location;

import java.io.Serializable;

public class LatLng implements Serializable {

    private double lat;
    private double lng;

    public static LatLng parseCommaSep(String str) {
        String[] split = str.split(",");
        return new LatLng(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
    }

    public LatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public LatLng(Location loc) {
        this(loc.getLatitude(), loc.getLongitude());
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String formatCommaSep() {
        return String.format("%f,%f", lat, lng);
    }

}
