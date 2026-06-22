package com.utils.services;

import com.utils.models.Notification;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

public class NotificationService {
    private final WeatherAPI weatherAPI;
    private final WeatherFormatter weatherFormatter;
    private final Map<Long, Notification> userNotifications = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public NotificationService(WeatherAPI weatherAPI, WeatherFormatter weatherFormatter) {
        this.weatherAPI = weatherAPI;
        this.weatherFormatter = weatherFormatter;
    }

    public String setNotification(long chatId, String city, String timeString) {
        try {
            // Проверяем формат времени
            if (!timeString.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                throw new IllegalArgumentException("Неверный формат времени");
            }

            LocalTime time = LocalTime.parse(timeString);

            // Проверяем, что город существует
            // (это вызовет исключение если город не найден)
            weatherAPI.getWeatherByCity(city, 1);

            // Отменяем существующее уведомление
            cancelNotification(chatId);

            // Создаем новое уведомление
            Notification notification = new Notification(chatId, city, time);
            userNotifications.put(chatId, notification);

            // Создаем задачу для уведомления
            scheduleNotification(chatId, notification);

            return String.format(
                    "✅ Уведомление установлено!\n" +
                            "🏙 Город: %s\n" +
                            "⏰ Время: %s\n\n" +
                            "Каждый день в это время вы будете получать прогноз погоды.",
                    city, time
            );

        } catch (Exception e) {
            return "❌ Ошибка: " + e.getMessage() +
                    "\nИспользуйте формат HH:MM и существующий город";
        }
    }

    public Notification getNotification(long chatId) {
        return userNotifications.get(chatId);
    }

    public String getWeatherNotification(long chatId) {
        Notification notification = userNotifications.get(chatId);
        if (notification == null) {
            return null;
        }

        try {
            String weather = weatherFormatter.getQuickWeather(notification.getCity());
            return String.format(
                    "🔔 Ежедневная погода для %s:\n\n%s",
                    notification.getCity(), weather
            );
        } catch (Exception e) {
            return String.format(
                    "❌ Ошибка при получении погоды для %s: %s",
                    notification.getCity(), e.getMessage()
            );
        }
    }

    public String cancelNotification(long chatId) {
        ScheduledFuture<?> task = scheduledTasks.get(chatId);
        if (task != null) {
            task.cancel(false);
            scheduledTasks.remove(chatId);
        }

        userNotifications.remove(chatId);
        return "❌ Уведомление отменено";
    }

    public String getNotificationInfo(long chatId) {
        Notification notification = userNotifications.get(chatId);
        if (notification == null) {
            return "❌ У вас нет активных уведомлений";
        }

        return String.format(
                "🔔 Активное уведомление:\nГород: %s\nВремя: %s",
                notification.getCity(),
                notification.getTime()
        );
    }

    private void scheduleNotification(long chatId, Notification notification) {
        LocalTime now = LocalTime.now();
        LocalTime targetTime = notification.getTime();

        long initialDelay = calculateInitialDelay(now, targetTime);

        // Создаем задачу, которая будет выполняться каждый день
        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(
                () -> {
                    // Задача просто запускается, отправкой сообщения занимается TelegramBot
                    System.out.println("Время отправить уведомление для chatId: " + chatId);
                },
                initialDelay,
                24 * 60 * 60 * 1000,
                TimeUnit.MILLISECONDS
        );

        scheduledTasks.put(chatId, task);
    }

    private long calculateInitialDelay(LocalTime now, LocalTime target) {
        long nowSeconds = now.toSecondOfDay();
        long targetSeconds = target.toSecondOfDay();

        long delay = targetSeconds - nowSeconds;

        if (delay < 0) {
            delay += 24 * 60 * 60;
        }

        return delay * 1000;
    }

    public boolean hasNotificationsToSend() {
        return !userNotifications.isEmpty();
    }

    public Set<Long> getActiveNotifications() {
        return userNotifications.keySet();
    }
}