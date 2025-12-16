package com.utils.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Daily {
    private List<String> time;
    
    @SerializedName("temperature_2m_max")
    private List<Double> temperature2mMax;

    @SerializedName("temperature_2m_min")
    private List<Double> temperature2mMin;

    @SerializedName("weathercode")
    private List<Integer> weatherCode;

    @SerializedName("windspeed_10m_max")
    private List<Double> windspeed10mMax;

    @SerializedName("precipitation_probability_max")
    private List<Double> precipitationProbabilityMax;

    public List<String> getTime() { return time; }
    public void setTime(List<String> time) { this.time = time; }
    public List<Double> getTemperature2mMax() { return temperature2mMax; }
    public void setTemperature2mMax(List<Double> temperature2mMax) { this.temperature2mMax = temperature2mMax; }
    public List<Double> getTemperature2mMin() { return temperature2mMin; }
    public void setTemperature2mMin(List<Double> temperature2mMin) { this.temperature2mMin = temperature2mMin; }
    public List<Integer> getWeatherCode() { return weatherCode; }
    public void setWeatherCode(List<Integer> weatherCode) { this.weatherCode = weatherCode; }
    public List<Double> getWindspeed10mMax() { return windspeed10mMax; }
    public void setWindspeed10mMax(List<Double> windspeed10mMax) { this.windspeed10mMax = windspeed10mMax; }
    public List<Double> getPrecipitationProbabilityMax() { return precipitationProbabilityMax; }
    public void setPrecipitationProbabilityMax(List<Double> precipitationProbabilityMax) { this.precipitationProbabilityMax = precipitationProbabilityMax; }
}