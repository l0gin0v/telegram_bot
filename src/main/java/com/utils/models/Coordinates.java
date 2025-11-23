package com.utils.models;

public class Coordinates {
    private double lat;
    private double lon;
    private String displayName;

    public Coordinates(double lat, double lon, String displayName) {
        this.lat = lat;
        this.lon = lon;
        this.displayName = displayName;
    }

    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public String getDisplayName() { return displayName; }

    public void setLat(double lat) { this.lat = lat; }
    public void setLon(double lon) { this.lon = lon; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    @Override
    public String toString() {
        return String.format("Coordinates{lat=%.4f, lon=%.4f, displayName='%s'}",
                lat, lon, displayName);
    }
}