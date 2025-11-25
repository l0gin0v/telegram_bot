package com.utils.models;

import java.util.List;

public class Daily {
    private List<String> time;
    private List<Double> temperature_2m_max;
    private List<Double> temperature_2m_min;
    private List<Integer> weathercode;
    private List<Double> windspeed_10m_max;
    private List<Double> precipitation_probability_max;

    public List<String> getTime() { return time; }
    public void setTime(List<String> time) { this.time = time; }
    public List<Double> getTemperature_2m_max() { return temperature_2m_max; }
    public void setTemperature_2m_max(List<Double> temperature_2m_max) { this.temperature_2m_max = temperature_2m_max; }
    public List<Double> getTemperature_2m_min() { return temperature_2m_min; }
    public void setTemperature_2m_min(List<Double> temperature_2m_min) { this.temperature_2m_min = temperature_2m_min; }
    public List<Integer> getWeathercode() { return weathercode; }
    public void setWeathercode(List<Integer> weathercode) { this.weathercode = weathercode; }
    public List<Double> getWindspeed_10m_max() { return windspeed_10m_max; }
    public void setWindspeed_10m_max(List<Double> windspeed_10m_max) { this.windspeed_10m_max = windspeed_10m_max; }
    public List<Double> getPrecipitation_probability_max() { return precipitation_probability_max; }
    public void setPrecipitation_probability_max(List<Double> precipitation_probability_max) { this.precipitation_probability_max = precipitation_probability_max; }
}