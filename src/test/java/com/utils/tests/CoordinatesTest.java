package com.utils.tests;

import com.utils.models.Coordinates;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CoordinatesTest {

    @Test
    void constructorAndGetters_ShouldSetAndReturnCorrectValues() {
        Coordinates coordinates = new Coordinates(55.7558, 37.6173, "Москва, Россия");

        assertEquals(55.7558, coordinates.getLat());
        assertEquals(37.6173, coordinates.getLon());
        assertEquals("Москва, Россия", coordinates.getDisplayName());
    }

    @Test
    void setters_ShouldUpdateValuesCorrectly() {
        Coordinates coordinates = new Coordinates(0, 0, "");

        coordinates.setLat(40.7128);
        coordinates.setLon(-74.0060);
        coordinates.setDisplayName("New York, USA");

        assertEquals(40.7128, coordinates.getLat());
        assertEquals(-74.0060, coordinates.getLon());
        assertEquals("New York, USA", coordinates.getDisplayName());
    }

    @Test
    void toString_ShouldReturnFormattedString() {
        Coordinates coordinates = new Coordinates(55.7558, 37.6173, "Москва");

        String result = coordinates.toString();

        assertTrue(result.contains("55.7558"));
        assertTrue(result.contains("37.6173"));
        assertTrue(result.contains("Москва"));
        assertTrue(result.startsWith("Coordinates{"));
    }

    @Test
    void negativeCoordinates_ShouldBeHandledCorrectly() {
        Coordinates coordinates = new Coordinates(-33.8688, 151.2093, "Сидней");

        assertEquals(-33.8688, coordinates.getLat());
        assertEquals(151.2093, coordinates.getLon());
    }

    @Test
    void zeroCoordinates_ShouldBeHandledCorrectly() {
        Coordinates coordinates = new Coordinates(0.0, 0.0, "Нулевая точка");

        assertEquals(0.0, coordinates.getLat());
        assertEquals(0.0, coordinates.getLon());
    }

    @Test
    void setDisplayNameToNull_ShouldBeHandled() {
        Coordinates coordinates = new Coordinates(1.0, 1.0, "Изначальное");
        coordinates.setDisplayName(null);

        assertNull(coordinates.getDisplayName());
    }
}