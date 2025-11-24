package com.utils.tests;

import com.utils.models.UserAnswerStatus;
import com.utils.services.WeatherAPI;
import com.utils.services.WeatherBotDialogLogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherBotDialogLogicTest {

    @Mock
    private WeatherAPI weatherAPI;

    private WeatherBotDialogLogic dialogLogic;

    @BeforeEach
    void setUp() {
        dialogLogic = new WeatherBotDialogLogic(weatherAPI);
    }

    @Test
    void getQuestion_ShouldReturnCityPrompt() {
        assertEquals("Введите название города для получения погоды:", dialogLogic.getQuestion());
    }

    @Test
    void needToStart_ShouldReturnStartCommand() {
        assertEquals("Для запуска бота введите /start", dialogLogic.needToStart());
    }

    @Test
    void welcomeWords_ShouldContainWelcomeMessage() {
        String result = dialogLogic.welcomeWords();

        assertAll(
                () -> assertTrue(result.contains("Добро пожаловать в погодный бот!")),
                () -> assertTrue(result.contains("/help")),
                () -> assertTrue(result.contains("/quit"))
        );
    }

    @Test
    void processAnswer_WithHelpCommand_ShouldReturnHelpMessage() {
        UserAnswerStatus status = dialogLogic.processAnswer("/help");

        assertAll(
                () -> assertFalse(status.isCorrectAnswer),
                () -> assertTrue(status.message.contains("Это погодный бот")),
                () -> assertFalse(status.isQuit)
        );
    }

    @Test
    void processAnswer_WithQuitCommand_ShouldReturnFarewellMessage() {
        UserAnswerStatus status = dialogLogic.processAnswer("/quit");

        assertAll(
                () -> assertFalse(status.isCorrectAnswer),
                () -> assertEquals("До свидания! Возвращайтесь еще!", status.message),
                () -> assertTrue(status.isQuit)
        );
    }

    @Test
    void processAnswer_WithValidCity_ShouldReturnWeather() throws Exception {
        when(weatherAPI.getQuickWeather("Москва")).thenReturn("☀️ +20°C");

        UserAnswerStatus status = dialogLogic.processAnswer("Москва");

        assertAll(
                () -> assertTrue(status.isCorrectAnswer),
                () -> assertEquals("☀️ +20°C", status.message),
                () -> assertFalse(status.isQuit)
        );
    }

    @Test
    void processAnswer_WithInvalidCity_ShouldReturnErrorMessage() throws Exception {
        when(weatherAPI.getQuickWeather("НесуществующийГород"))
                .thenThrow(new RuntimeException("Город не найден"));

        UserAnswerStatus status = dialogLogic.processAnswer("НесуществующийГород");

        assertAll(
                () -> assertFalse(status.isCorrectAnswer),
                () -> assertTrue(status.message.contains("Не удалось получить погоду")),
                () -> assertFalse(status.isQuit)
        );
    }

    @Test
    void getWeatherForPeriod_WithToday_ShouldReturnTodayWeather() throws Exception {
        when(weatherAPI.getFormattedWeatherByCity("Москва", 1))
                .thenReturn("Погода сегодня: ☀️ +20°C");

        String result = dialogLogic.getWeatherForPeriod("Москва", "today");

        assertEquals("Погода сегодня: ☀️ +20°C", result);
    }

    @Test
    void getWeatherForPeriod_WithTomorrow_ShouldReturnTomorrowWeather() throws Exception {
        when(weatherAPI.getFormattedWeatherByCity("Москва", 2))
                .thenReturn("Погода завтра: 🌧 +15°C");

        String result = dialogLogic.getWeatherForPeriod("Москва", "tomorrow");

        assertEquals("Погода завтра: 🌧 +15°C", result);
    }

    @Test
    void getWeatherForPeriod_With3Days_ShouldReturn3DaysWeather() throws Exception {
        when(weatherAPI.getFormattedWeatherByCity("Москва", 3))
                .thenReturn("Погода на 3 дня: ⛅ +18°C");

        String result = dialogLogic.getWeatherForPeriod("Москва", "3days");

        assertEquals("Погода на 3 дня: ⛅ +18°C", result);
    }

    @Test
    void getWeatherForPeriod_WithWeek_ShouldReturnWeekWeather() throws Exception {
        when(weatherAPI.getFormattedWeatherByCity("Москва", 7))
                .thenReturn("Погода на неделю: 🌦 +17°C");

        String result = dialogLogic.getWeatherForPeriod("Москва", "week");

        assertEquals("Погода на неделю: 🌦 +17°C", result);
    }

    @Test
    void getWeatherForPeriod_WithDefault_ShouldReturnQuickWeather() throws Exception {
        when(weatherAPI.getQuickWeather("Москва")).thenReturn("☀️ +20°C");

        String result = dialogLogic.getWeatherForPeriod("Москва", "unknown");

        assertEquals("☀️ +20°C", result);
    }

    @Test
    void getWeatherForPeriod_WithException_ShouldReturnErrorMessage() throws Exception {
        when(weatherAPI.getFormattedWeatherByCity("Москва", 1))
                .thenThrow(new RuntimeException("API недоступно"));

        String result = dialogLogic.getWeatherForPeriod("Москва", "today");

        assertTrue(result.contains("❌ Ошибка при получении погоды"));
    }
}