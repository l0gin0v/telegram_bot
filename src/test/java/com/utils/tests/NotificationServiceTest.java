package com.utils.tests;

import com.utils.services.NotificationService;
import com.utils.services.WeatherAPI;
import com.utils.services.WeatherFormatter;
import com.utils.models.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private WeatherAPI mockWeatherAPI;

    @Mock
    private WeatherFormatter mockWeatherFormatter;

    @Mock
    private ScheduledExecutorService mockScheduler;

    @Mock
    private ScheduledFuture mockScheduledFuture; // Убрали <?> для избежания проблем с дженериками

    private NotificationService notificationService;

    @BeforeEach
    void setUp() throws Exception {
        notificationService = new NotificationService(mockWeatherAPI, mockWeatherFormatter);
        setPrivateField(notificationService, "scheduler", mockScheduler);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    @Test
    void setNotification_WithValidInput_ShouldScheduleNotification() throws Exception {
        when(mockScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
                .thenReturn(mockScheduledFuture);

        String result = notificationService.setNotification(123L, "Москва", "09:00");

        assertTrue(result.contains("✅ Уведомление установлено"));
        assertTrue(result.contains("Москва"));
        assertTrue(result.contains("09:00"));

        Map<Long, Notification> notifications = getPrivateField(notificationService, "userNotifications");
        assertTrue(notifications.containsKey(123L));

        Notification notification = notifications.get(123L);
        assertEquals("Москва", notification.getCity());
        assertEquals(LocalTime.parse("09:00"), notification.getTime());
        assertTrue(notification.isActive());

        Map<Long, ScheduledFuture<?>> scheduledTasks = getPrivateField(notificationService, "scheduledTasks");
        assertTrue(scheduledTasks.containsKey(123L));

        verify(mockWeatherAPI, times(1)).getWeatherByCity("Москва", 1);
        verify(mockScheduler, times(1)).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any());
    }

    @Test
    void setNotification_WithInvalidCity_ShouldReturnError() throws Exception {
        when(mockWeatherAPI.getWeatherByCity("НесуществующийГород", 1))
                .thenThrow(new RuntimeException("Город не найден"));

        String result = notificationService.setNotification(123L, "НесуществующийГород", "09:00");

        assertTrue(result.contains("❌ Ошибка"));
        assertTrue(result.contains("существующий город"));

        verify(mockWeatherAPI, times(1)).getWeatherByCity("НесуществующийГород", 1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"00:00", "12:30", "23:59", "09:15", "18:45"})
    void setNotification_WithDifferentValidTimes_ShouldWork(String time) throws Exception {
        when(mockScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
                .thenReturn(mockScheduledFuture);

        String result = notificationService.setNotification(123L, "Москва", time);

        assertTrue(result.contains("✅ Уведомление установлено"));
        assertTrue(result.contains(time));
    }

    @Test
    void setNotification_WhenReplacingExisting_ShouldCancelPrevious() throws Exception {
        when(mockScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
                .thenReturn(mockScheduledFuture);

        notificationService.setNotification(123L, "Москва", "09:00");
        String result = notificationService.setNotification(123L, "Санкт-Петербург", "10:00");

        Map<Long, Notification> notifications = getPrivateField(notificationService, "userNotifications");
        Notification notification = notifications.get(123L);

        assertEquals("Санкт-Петербург", notification.getCity());
        assertEquals(LocalTime.parse("10:00"), notification.getTime());
        assertTrue(result.contains("✅ Уведомление установлено"));
        assertTrue(result.contains("Санкт-Петербург"));

        verify(mockScheduler, times(2)).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any());
    }

    @Test
    void getNotification_ShouldReturnCorrectNotification() throws Exception {
        when(mockScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
                .thenReturn(mockScheduledFuture);

        notificationService.setNotification(123L, "Москва", "09:00");

        Notification notification = notificationService.getNotification(123L);
        assertNotNull(notification);
        assertEquals(123L, notification.getChatId());
        assertEquals("Москва", notification.getCity());
        assertEquals(LocalTime.parse("09:00"), notification.getTime());

        Notification nonExistent = notificationService.getNotification(999L);
        assertNull(nonExistent);
    }

    @Test
    void getWeatherNotification_ShouldReturnFormattedWeather() throws Exception {
        when(mockScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
                .thenReturn(mockScheduledFuture);
        when(mockWeatherFormatter.getQuickWeather("Москва"))
                .thenReturn("☀️ +20°C, ясно");

        notificationService.setNotification(123L, "Москва", "09:00");

        String result = notificationService.getWeatherNotification(123L);

        assertTrue(result.contains("🔔 Ежедневная погода"));
        assertTrue(result.contains("Москва"));
        assertTrue(result.contains("☀️ +20°C"));

        verify(mockWeatherFormatter, times(1)).getQuickWeather("Москва");
    }

    @Test
    void getWeatherNotification_WithError_ShouldReturnErrorMessage() throws Exception {
        when(mockScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
                .thenReturn(mockScheduledFuture);
        when(mockWeatherFormatter.getQuickWeather("Москва"))
                .thenThrow(new RuntimeException("API error"));

        notificationService.setNotification(123L, "Москва", "09:00");

        String result = notificationService.getWeatherNotification(123L);

        assertTrue(result.contains("❌ Ошибка"));
        assertTrue(result.contains("Москва"));
        assertTrue(result.contains("API error"));
    }

    @Test
    void getWeatherNotification_WithoutNotification_ShouldReturnNull() {
        String result = notificationService.getWeatherNotification(123L);
        assertNull(result);
    }

    @Test
    void cancelNotification_WithoutExistingNotification_ShouldWork() {
        String result = notificationService.cancelNotification(123L);
        assertEquals("❌ Уведомление отменено", result);
    }

    @Test
    void getNotificationInfo_WithActiveNotification_ShouldReturnInfo() throws Exception {
        when(mockScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
                .thenReturn(mockScheduledFuture);

        notificationService.setNotification(123L, "Москва", "09:00");

        String result = notificationService.getNotificationInfo(123L);

        assertTrue(result.contains("🔔 Активное уведомление"));
        assertTrue(result.contains("Город: Москва"));
        assertTrue(result.contains("Время: 09:00"));
    }

    @Test
    void getNotificationInfo_WithoutNotification_ShouldReturnErrorMessage() {
        String result = notificationService.getNotificationInfo(123L);
        assertEquals("❌ У вас нет активных уведомлений", result);
    }

    @Test
    void hasNotificationsToSend_ShouldReturnCorrectStatus() throws Exception {
        when(mockScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
                .thenReturn(mockScheduledFuture);

        assertFalse(notificationService.hasNotificationsToSend());

        notificationService.setNotification(123L, "Москва", "09:00");

        assertTrue(notificationService.hasNotificationsToSend());
    }

    @Test
    void getActiveNotifications_ShouldReturnAllChatIds() throws Exception {
        when(mockScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
                .thenReturn(mockScheduledFuture);

        Set<Long> activeNotifications = notificationService.getActiveNotifications();
        assertTrue(activeNotifications.isEmpty());

        notificationService.setNotification(123L, "Москва", "09:00");
        notificationService.setNotification(456L, "Санкт-Петербург", "10:00");

        activeNotifications = notificationService.getActiveNotifications();
        assertEquals(2, activeNotifications.size());
        assertTrue(activeNotifications.contains(123L));
        assertTrue(activeNotifications.contains(456L));
    }

    @Test
    void scheduleNotification_ShouldBeCalled() throws Exception {
        when(mockScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
                .thenReturn(mockScheduledFuture);

        notificationService.setNotification(123L, "Москва", "09:00");

        verify(mockScheduler, times(1)).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any());
    }

    @Test
    void multipleUsers_ShouldHaveIndependentNotifications() throws Exception {
        when(mockScheduler.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any()))
                .thenReturn(mockScheduledFuture);

        notificationService.setNotification(111L, "Москва", "09:00");
        notificationService.setNotification(222L, "Санкт-Петербург", "10:00");
        notificationService.setNotification(333L, "Новосибирск", "11:00");

        Set<Long> activeNotifications = notificationService.getActiveNotifications();
        assertEquals(3, activeNotifications.size());

        notificationService.cancelNotification(222L);

        activeNotifications = notificationService.getActiveNotifications();
        assertEquals(2, activeNotifications.size());
        assertTrue(activeNotifications.contains(111L));
        assertTrue(activeNotifications.contains(333L));
        assertFalse(activeNotifications.contains(222L));
    }
}