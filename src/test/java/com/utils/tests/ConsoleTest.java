package com.utils.tests;

import com.utils.interfaces.IDialogLogic;
import com.utils.models.UserAnswerStatus;
import com.utils.services.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsoleTest {

    @Mock
    private IDialogLogic dialogLogic;

    @Mock
    private WeatherAPI weatherAPI;

    @Mock
    private WeatherFormatter weatherFormatter;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationScheduler notificationScheduler;

    private Console console;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private final ByteArrayInputStream[] inputStreamHolder = new ByteArrayInputStream[1];

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(System.in);
        if (console != null) {
            console = null;
        }
    }

    private void setInput(String input) {
        inputStreamHolder[0] = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStreamHolder[0]);
    }

    @Test
    void runBot_WaitsForStartCommand() throws Exception {
        // Arrange
        String input = "wrong\ninvalid\n/start\n/quit\n";
        setInput(input);

        when(dialogLogic.needToStart()).thenReturn("Введите /start");
        when(dialogLogic.welcomeWords()).thenReturn("Started");
        when(dialogLogic.getQuestion()).thenReturn("Q");
        when(dialogLogic.processAnswer("/quit")).thenReturn(new UserAnswerStatus(false, "Bye", true));

        try (MockedConstruction<WeatherAPI> mockedWeatherAPI = mockConstruction(WeatherAPI.class);
             MockedConstruction<WeatherFormatter> mockedFormatter = mockConstruction(WeatherFormatter.class);
             MockedConstruction<NotificationService> mockedService = mockConstruction(NotificationService.class);
             MockedConstruction<NotificationScheduler> mockedScheduler = mockConstruction(NotificationScheduler.class)) {

            // Act
            console = new Console(dialogLogic);
            Thread botThread = new Thread(() -> console.runBot());
            botThread.start();
            Thread.sleep(500);
            botThread.interrupt();

            // Assert
            String output = outputStream.toString();
            assertTrue(output.contains("Введите /start"));
        }
    }

    @Test
    void runBot_HandlesNotificationMenu() throws Exception {
        // Arrange
        String input = "/start\nответ\nда\n5\n/quit\n";
        setInput(input);

        when(dialogLogic.welcomeWords()).thenReturn("Welcome");
        when(dialogLogic.getQuestion()).thenReturn("Question");
        when(dialogLogic.processAnswer("ответ")).thenReturn(
                new UserAnswerStatus(true, "Правильно! Город установлен: Москва\nОтлично!", false)
        );
        when(dialogLogic.processAnswer("/quit")).thenReturn(new UserAnswerStatus(false, "Bye", true));

        try (MockedConstruction<WeatherAPI> mockedWeatherAPI = mockConstruction(WeatherAPI.class);
             MockedConstruction<WeatherFormatter> mockedFormatter = mockConstruction(WeatherFormatter.class);
             MockedConstruction<NotificationService> mockedService = mockConstruction(NotificationService.class,
                     (mock, context) -> {
                         when(mock.setNotification(anyLong(), anyString(), anyString()))
                                 .thenReturn("Уведомление настроено");
                         when(mock.getNotificationInfo(anyLong()))
                                 .thenReturn("Информация об уведомлениях");
                         when(mock.cancelNotification(anyLong()))
                                 .thenReturn("Уведомление отменено");
                         when(mock.getWeatherNotification(anyLong()))
                                 .thenReturn("Погода в Москве: +20°C, солнечно");
                     });
             MockedConstruction<NotificationScheduler> mockedScheduler = mockConstruction(NotificationScheduler.class)) {

            // Act
            console = new Console(dialogLogic);
            Thread botThread = new Thread(() -> console.runBot());
            botThread.start();
            Thread.sleep(500);
            botThread.interrupt();

            // Assert
            String output = outputStream.toString();
            assertTrue(output.contains("Город 'Москва' сохранен для уведомлений"));
            assertTrue(output.contains("УПРАВЛЕНИЕ УВЕДОМЛЕНИЯМИ"));
        }
    }

    @Test
    void extractCityFromResponse_SavesCurrentCity() throws Exception {
        // Arrange
        String input = "/start\nответ\nнет\n/quit\n";
        setInput(input);

        when(dialogLogic.welcomeWords()).thenReturn("Welcome");
        when(dialogLogic.getQuestion()).thenReturn("Question");
        when(dialogLogic.processAnswer("ответ")).thenReturn(
                new UserAnswerStatus(true, "Правильно! Город установлен: Санкт-Петербург\nОтлично!", false)
        );
        when(dialogLogic.processAnswer("/quit")).thenReturn(new UserAnswerStatus(false, "Bye", true));

        try (MockedConstruction<WeatherAPI> mockedWeatherAPI = mockConstruction(WeatherAPI.class);
             MockedConstruction<WeatherFormatter> mockedFormatter = mockConstruction(WeatherFormatter.class);
             MockedConstruction<NotificationService> mockedService = mockConstruction(NotificationService.class);
             MockedConstruction<NotificationScheduler> mockedScheduler = mockConstruction(NotificationScheduler.class)) {

            // Act
            console = new Console(dialogLogic);
            Thread botThread = new Thread(() -> console.runBot());
            botThread.start();
            Thread.sleep(500);
            botThread.interrupt();

            // Assert
            assertEquals("Санкт-Петербург", console.getCurrentCity());
        }
    }

    @Test
    void sendNotificationToUser_SendsToConsole() throws Exception {
        // Arrange
        String notificationText = "Погода в Москве: +20°C, солнечно";

        try (MockedConstruction<WeatherAPI> mockedWeatherAPI = mockConstruction(WeatherAPI.class);
             MockedConstruction<WeatherFormatter> mockedFormatter = mockConstruction(WeatherFormatter.class);
             MockedConstruction<NotificationService> mockedService = mockConstruction(NotificationService.class);
             MockedConstruction<NotificationScheduler> mockedScheduler = mockConstruction(NotificationScheduler.class)) {

            // Act
            console = new Console(dialogLogic);
            console.sendNotificationToUser(1L, notificationText);

            // Assert
            String output = outputStream.toString();
            assertTrue(output.contains("🔔 ЕЖЕДНЕВНОЕ УВЕДОМЛЕНИЕ"));
            assertTrue(output.contains(notificationText));

            // Проверяем, что для другого ID не отправляется
            outputStream.reset();
            console.sendNotificationToUser(2L, notificationText);
            assertFalse(outputStream.toString().contains("🔔 ЕЖЕДНЕВНОЕ УВЕДОМЛЕНИЕ"));
        }
    }

    @Test
    void handleNotificationMenu_SetNotificationTime() throws Exception {
        // Arrange
        String input = "/start\nответ\nда\n1\n09:00\n5\n/quit\n";
        setInput(input);

        when(dialogLogic.welcomeWords()).thenReturn("Welcome");
        when(dialogLogic.getQuestion()).thenReturn("Question");
        when(dialogLogic.processAnswer("ответ")).thenReturn(
                new UserAnswerStatus(true, "Правильно! Город установлен: Москва\nОтлично!", false)
        );
        when(dialogLogic.processAnswer("/quit")).thenReturn(new UserAnswerStatus(false, "Bye", true));

        try (MockedConstruction<WeatherAPI> mockedWeatherAPI = mockConstruction(WeatherAPI.class);
             MockedConstruction<WeatherFormatter> mockedFormatter = mockConstruction(WeatherFormatter.class);
             MockedConstruction<NotificationService> mockedService = mockConstruction(NotificationService.class,
                     (mock, context) -> {
                         when(mock.setNotification(1L, "Москва", "09:00"))
                                 .thenReturn("Уведомление настроено на 09:00");
                         when(mock.getNotificationInfo(1L))
                                 .thenReturn("Уведомления активны для Москвы в 09:00");
                         when(mock.cancelNotification(1L))
                                 .thenReturn("Уведомления отключены");
                     });
             MockedConstruction<NotificationScheduler> mockedScheduler = mockConstruction(NotificationScheduler.class)) {

            // Act
            console = new Console(dialogLogic);
            Thread botThread = new Thread(() -> console.runBot());
            botThread.start();
            Thread.sleep(500);
            botThread.interrupt();

            // Assert
            String output = outputStream.toString();
            assertTrue(output.contains("Время: 09:00"));
        }
    }

    @Test
    void handleNotificationMenu_InvalidTimeFormat() throws Exception {
        // Arrange
        String input = "/start\nответ\nда\n1\ninvalid\n5\n/quit\n";
        setInput(input);

        when(dialogLogic.welcomeWords()).thenReturn("Welcome");
        when(dialogLogic.getQuestion()).thenReturn("Question");
        when(dialogLogic.processAnswer("ответ")).thenReturn(
                new UserAnswerStatus(true, "Правильно! Город установлен: Москва\nОтлично!", false)
        );
        when(dialogLogic.processAnswer("/quit")).thenReturn(new UserAnswerStatus(false, "Bye", true));

        try (MockedConstruction<WeatherAPI> mockedWeatherAPI = mockConstruction(WeatherAPI.class);
             MockedConstruction<WeatherFormatter> mockedFormatter = mockConstruction(WeatherFormatter.class);
             MockedConstruction<NotificationService> mockedService = mockConstruction(NotificationService.class);
             MockedConstruction<NotificationScheduler> mockedScheduler = mockConstruction(NotificationScheduler.class)) {

            // Act
            console = new Console(dialogLogic);
            Thread botThread = new Thread(() -> console.runBot());
            botThread.start();
            Thread.sleep(500);
            botThread.interrupt();

            // Assert
            String output = outputStream.toString();
            assertTrue(output.contains("Неверный формат времени"));
        }
    }

    @Test
    void testNotification_SendsTestNotification() throws Exception {
        // Arrange
        String input = "/start\nответ\nда\n4\n5\n/quit\n";
        setInput(input);

        when(dialogLogic.welcomeWords()).thenReturn("Welcome");
        when(dialogLogic.getQuestion()).thenReturn("Question");
        when(dialogLogic.processAnswer("ответ")).thenReturn(
                new UserAnswerStatus(true, "Правильно! Город установлен: Москва\nОтлично!", false)
        );
        when(dialogLogic.processAnswer("/quit")).thenReturn(new UserAnswerStatus(false, "Bye", true));

        try (MockedConstruction<WeatherAPI> mockedWeatherAPI = mockConstruction(WeatherAPI.class);
             MockedConstruction<WeatherFormatter> mockedFormatter = mockConstruction(WeatherFormatter.class);
             MockedConstruction<NotificationService> mockedService = mockConstruction(NotificationService.class,
                     (mock, context) -> {
                         when(mock.getWeatherNotification(1L))
                                 .thenReturn("Погода в Москве: +20°C, солнечно");
                         when(mock.getNotificationInfo(1L))
                                 .thenReturn("Уведомления активны");
                     });
             MockedConstruction<NotificationScheduler> mockedScheduler = mockConstruction(NotificationScheduler.class)) {

            // Act
            console = new Console(dialogLogic);
            Thread botThread = new Thread(() -> console.runBot());
            botThread.start();
            Thread.sleep(500);
            botThread.interrupt();

            // Assert
            String output = outputStream.toString();
            assertTrue(output.contains("[ТЕСТ]"));
            assertTrue(output.contains("Тестовое уведомление отправлено"));
        }
    }

    @Test
    void getClientName_ReturnsConsoleBot() throws Exception {
        // Arrange
        try (MockedConstruction<WeatherAPI> mockedWeatherAPI = mockConstruction(WeatherAPI.class);
             MockedConstruction<WeatherFormatter> mockedFormatter = mockConstruction(WeatherFormatter.class);
             MockedConstruction<NotificationService> mockedService = mockConstruction(NotificationService.class);
             MockedConstruction<NotificationScheduler> mockedScheduler = mockConstruction(NotificationScheduler.class)) {

            // Act
            console = new Console(dialogLogic);

            // Assert
            assertEquals("ConsoleBot", console.getClientName());
        }
    }
}