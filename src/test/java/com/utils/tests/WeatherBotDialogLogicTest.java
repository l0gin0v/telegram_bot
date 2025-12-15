package com.utils.tests;

import com.utils.services.*;
import com.utils.models.UserAnswerStatus;
import com.utils.models.Coordinates;
import com.utils.models.OpenMeteoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WeatherBotDialogLogicTest {

    @Mock
    private WeatherAPI mockWeatherAPI;

    @Mock
    private WeatherFormatter mockWeatherFormatter;

    @Mock
    private Geocoding mockGeocoding;

    private WeatherBotDialogLogic weatherBotDialogLogic;

    @BeforeEach
    void setUp() {
        weatherBotDialogLogic = new WeatherBotDialogLogic(mockWeatherAPI);

        // Используем рефлексию дляодмены weatherFormatter на мок
        try {
            var baseClass = weatherBotDialogLogic.getClass().getSuperclass();
            var field = baseClass.getDeclaredField("weatherFormatter");
            field.setAccessible(true);
            field.set(weatherBotDialogLogic, mockWeatherFormatter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(mockWeatherAPI.getGeocoding()).thenReturn(mockGeocoding);
    }

    @Test
    void getQuestion_shouldAskForCity_whenNoCityIsSet() {
        // Act
        String result = weatherBotDialogLogic.getQuestion();

        // Assert
        assertTrue(result.contains("Введите название города"));
    }

    @Test
    void welcomeWords_shouldReturnWelcomeMessage() {
        // Act
        String result = weatherBotDialogLogic.welcomeWords();

        // Assert
        assertTrue(result.contains("Добро пожаловать"));
    }

    @Test
    void farewallWordsForInactive_shouldReturnInactiveMessage() {
        // Act
        String result = weatherBotDialogLogic.farewallWordsForInactive();

        // Assert
        assertTrue(result.contains("сессия истекла"));
        assertTrue(result.contains("/start"));
    }

    @Test
    void processAnswer_shouldSetCity_whenValidCityEntered() throws Exception {
        // Arrange
        String city = "Moscow";
        String weatherResponse = "☀️ Погода в Москве: 20°C";
        when(mockWeatherFormatter.getQuickWeather(city)).thenReturn(weatherResponse);

        // Act
        UserAnswerStatus result = weatherBotDialogLogic.processAnswer(city);

        // Assert
        assertTrue(result.isCorrectAnswer);
        assertTrue(result.message.contains("Город установлен: Moscow"));
        assertTrue(result.message.contains(weatherResponse));
        verify(mockWeatherFormatter).getQuickWeather(city);
    }

    @Test
    void processAnswer_shouldReturnError_whenInvalidCityEntered() throws Exception {
        // Arrange
        String invalidCity = "InvalidCity123";
        when(mockWeatherFormatter.getQuickWeather(invalidCity))
                .thenThrow(new RuntimeException("City not found"));

        // Act
        UserAnswerStatus result = weatherBotDialogLogic.processAnswer(invalidCity);

        // Assert
        assertFalse(result.isCorrectAnswer);
        assertTrue(result.message.contains("Не удалось получить погоду"));
    }

    @Test
    void getWeatherForPeriod_shouldReturnWeatherForToday() throws Exception {
        // Arrange
        String city = "Moscow";
        String todayWeather = "Today's weather in Moscow";
        when(mockWeatherFormatter.getQuickWeather(city)).thenReturn(todayWeather);
        weatherBotDialogLogic.processAnswer(city);

        // Act
        String result = weatherBotDialogLogic.getWeatherForPeriod(city, 1);

        // Assert
        assertEquals(todayWeather, result);
    }

    @Test
    void getWeatherForPeriod_shouldReturnWeatherForTomorrow() throws Exception {
        // Arrange
        String city = "Moscow";
        String tomorrowWeather = "Tomorrow's weather";
        when(mockWeatherFormatter.formatTomorrowWeather(city)).thenReturn(tomorrowWeather);

        // Act
        String result = weatherBotDialogLogic.getWeatherForPeriod(city, 2);

        // Assert
        assertEquals(tomorrowWeather, result);
    }

    @Test
    void getHelp_shouldReturnHelpText() {
        // Act
        String helpText = weatherBotDialogLogic.getHelp();

        // Assert
        assertTrue(helpText.contains("Погодный бот - справка"));
        assertTrue(helpText.contains("Введите название города"));
    }

    @Test
    void processAnswer_shouldHandleMenuOptions() throws Exception {
        // Arrange
        String city = "Moscow";
        when(mockWeatherFormatter.getQuickWeather(city)).thenReturn("Weather");
        weatherBotDialogLogic.processAnswer(city);

        String todayWeather = "Today's weather";
        when(mockWeatherFormatter.getQuickWeather(city)).thenReturn(todayWeather);
        UserAnswerStatus result1 = weatherBotDialogLogic.processAnswer("1");
        assertEquals(todayWeather, result1.message);

        String tomorrowWeather = "Tomorrow's weather";
        when(mockWeatherFormatter.formatTomorrowWeather(city)).thenReturn(tomorrowWeather);
        UserAnswerStatus result2 = weatherBotDialogLogic.processAnswer("2");
        assertEquals(tomorrowWeather, result2.message);

        UserAnswerStatus result5 = weatherBotDialogLogic.processAnswer("5");
        assertFalse(result5.isCorrectAnswer);
        assertTrue(result5.message.contains("Введите новый город"));
    }
}