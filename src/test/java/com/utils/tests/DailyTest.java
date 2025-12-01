package com.utils.tests;

import com.utils.models.Daily;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DailyTest {

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        Daily daily = new Daily();

        List<String> time = Arrays.asList("2025-10-01", "2025-10-02", "2025-10-03");
        List<Double> tempMax = Arrays.asList(25.5, 26.0, 24.5);
        List<Double> tempMin = Arrays.asList(15.5, 16.0, 14.5);
        List<Integer> weatherCode = Arrays.asList(0, 1, 2);
        List<Double> windSpeed = Arrays.asList(10.5, 12.0, 8.5);
        List<Double> precipitationProb = Arrays.asList(30.0, 20.0, 40.0);

        daily.setTime(time);
        daily.setTemperature2mMax(tempMax);
        daily.setTemperature2mMin(tempMin);
        daily.setWeathercode(weatherCode);
        daily.setWindspeed10mMax(windSpeed);
        daily.setPrecipitationProbabilityMax(precipitationProb);

        assertEquals(time, daily.getTime());
        assertEquals(tempMax, daily.getTemperature2mMax());
        assertEquals(tempMin, daily.getTemperature2mMin());
        assertEquals(weatherCode, daily.getWeathercode());
        assertEquals(windSpeed, daily.getWindspeed10mMax());
        assertEquals(precipitationProb, daily.getPrecipitationProbabilityMax());
    }

    @Test
    void settersWithNullValues_ShouldWorkCorrectly() {
        Daily daily = new Daily();

        daily.setTime(null);
        daily.setTemperature2mMax(null);
        daily.setTemperature2mMin(null);
        daily.setWeathercode(null);
        daily.setWindspeed10mMax(null);
        daily.setPrecipitationProbabilityMax(null);

        assertNull(daily.getTime());
        assertNull(daily.getTemperature2mMax());
        assertNull(daily.getTemperature2mMin());
        assertNull(daily.getWeathercode());
        assertNull(daily.getWindspeed10mMax());
        assertNull(daily.getPrecipitationProbabilityMax());
    }

    @Test
    void settersWithEmptyLists_ShouldWorkCorrectly() {
        Daily daily = new Daily();

        List<String> emptyTime = new ArrayList<>();
        List<Double> emptyTemp = new ArrayList<>();
        List<Integer> emptyCode = new ArrayList<>();

        daily.setTime(emptyTime);
        daily.setTemperature2mMax(emptyTemp);
        daily.setTemperature2mMin(emptyTemp);
        daily.setWeathercode(emptyCode);
        daily.setWindspeed10mMax(emptyTemp);
        daily.setPrecipitationProbabilityMax(emptyTemp);

        assertEquals(0, daily.getTime().size());
        assertEquals(0, daily.getTemperature2mMax().size());
        assertEquals(0, daily.getTemperature2mMin().size());
        assertEquals(0, daily.getWeathercode().size());
        assertEquals(0, daily.getWindspeed10mMax().size());
        assertEquals(0, daily.getPrecipitationProbabilityMax().size());
    }

    @Test
    void fieldValues_ShouldBeIndependentAfterSetting() {
        Daily daily = new Daily();

        List<String> originalTime = new ArrayList<>(Arrays.asList("2025-10-01", "2025-10-02"));
        List<Double> originalTemp = new ArrayList<>(Arrays.asList(25.5, 26.0));

        daily.setTime(originalTime);
        daily.setTemperature2mMax(originalTemp);

        List<String> modifiedTime = new ArrayList<>(daily.getTime());
        List<Double> modifiedTemp = new ArrayList<>(daily.getTemperature2mMax());

        modifiedTime.add("2025-10-04");
        modifiedTemp.add(27.0);

        assertEquals(3, modifiedTime.size());
        assertEquals(3, modifiedTemp.size());

        assertEquals(2, daily.getTime().size());
        assertEquals(2, daily.getTemperature2mMax().size());
    }

    @Test
    void multipleUpdates_ShouldKeepLastValue() {
        Daily daily = new Daily();

        List<String> time1 = Arrays.asList("2025-10-01");
        List<String> time2 = Arrays.asList("2025-10-02", "2025-10-03");

        daily.setTime(time1);
        daily.setTime(time2);

        assertEquals(2, daily.getTime().size());
        assertEquals("2025-10-02", daily.getTime().get(0));
        assertEquals("2025-10-03", daily.getTime().get(1));
    }
}