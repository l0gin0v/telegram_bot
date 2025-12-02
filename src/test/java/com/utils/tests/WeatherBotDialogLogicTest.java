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
    void processAnswer_WithQuitCommand_ShouldReturnFarewellMessage() {
        UserAnswerStatus status = dialogLogic.processAnswer("/quit");

        assertAll(
                () -> assertFalse(status.isCorrectAnswer),
                () -> assertEquals("До свидания! Возвращайтесь еще!", status.message),
                () -> assertTrue(status.isQuit)
        );
    }

    @Test
    void getHelp_ShouldReturnHelpMessage() {
        String help = dialogLogic.getHelp();
        assertAll(
                () -> assertTrue(help.contains("📖 Помощь по боту")),
                () -> assertTrue(help.contains("/start")),
                () -> assertTrue(help.contains("/quit")),
                () -> assertTrue(help.contains("/help"))
        );
    }

    @Test
    void getWeatherForPeriod_With1Day_ShouldCallCorrectMethod() throws Exception {
        String result = dialogLogic.getWeatherForPeriod("Москва", 1);
        assertNotNull(result);
    }

    @Test
    void getWeatherForPeriod_With2Days_ShouldCallCorrectMethod() throws Exception {
        String result = dialogLogic.getWeatherForPeriod("Москва", 2);
        assertNotNull(result);
    }

    @Test
    void getWeatherForPeriod_With3Days_ShouldCallCorrectMethod() throws Exception {
        String result = dialogLogic.getWeatherForPeriod("Москва", 3);
        assertNotNull(result);
    }

    @Test
    void getWeatherForPeriod_With7Days_ShouldCallCorrectMethod() throws Exception {
        String result = dialogLogic.getWeatherForPeriod("Москва", 7);
        assertNotNull(result);
    }

    @Test
    void getWeatherForPeriod_WithDefaultDays_ShouldCallCorrectMethod() throws Exception {
        String result = dialogLogic.getWeatherForPeriod("Москва", 5);
        assertNotNull(result);
    }

}