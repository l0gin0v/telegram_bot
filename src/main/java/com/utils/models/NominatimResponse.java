package com.utils.models;

import com.google.gson.annotations.SerializedName;

public class NominatimResponse {
    @SerializedName("lat")
    private String latitude;

    @SerializedName("lon")
    private String longitude;

    @SerializedName("display_name")
    private String displayName;

    private String type;

    private double importance;

    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getImportance() { return importance; }
    public void setImportance(double importance) { this.importance = importance; }
}