package com.utils.services;

import com.utils.models.Notification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationScheduler implements Runnable {
    private final NotificationService notificationService;
    private final TelegramBot telegramBot;
    private final ConcurrentHashMap<Long, LocalDate> lastNotificationSent = new ConcurrentHashMap<>();

    public NotificationScheduler(NotificationService notificationService, TelegramBot telegramBot) {
        this.notificationService = notificationService;
        this.telegramBot = telegramBot;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                checkAndSendNotifications();
                Thread.sleep(30000); // Проверка каждые 30 секунд
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Ошибка в NotificationScheduler: " + e.getMessage());
            }
        }
    }

    private void checkAndSendNotifications() {
        try {
            Set<Long> activeChats = notificationService.getActiveNotifications();

            for (Long chatId : activeChats) {
                // Проверяем активна ли сессия пользователя через телеграм бот
                if (telegramBot.isUserSessionActive(chatId)) {
                    Notification notification = notificationService.getNotification(chatId);
                    if (notification == null) continue;

                    LocalDate today = LocalDate.now();
                    LocalDate lastSent = lastNotificationSent.get(chatId);

                    if (lastSent != null && lastSent.equals(today)) {
                        continue; // Уже отправляли сегодня
                    }

                    LocalTime now = LocalTime.now();
                    LocalTime notificationTime = notification.getTime();

                    if (isTimeToSend(now, notificationTime)) {
                        String notificationText = notificationService.getWeatherNotification(chatId);

                        if (notificationText != null && !notificationText.startsWith("❌")) {
                            // Используем метод телеграм бота для отправки сообщения
                            telegramBot.sendNotificationToUser(chatId, notificationText);
                            lastNotificationSent.put(chatId, today);
                            System.out.println("Отправлено уведомление для chatId: " + chatId + " в " + now);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при проверке уведомлений: " + e.getMessage());
        }
    }

    private boolean isTimeToSend(LocalTime now, LocalTime notificationTime) {
        long nowSeconds = now.toSecondOfDay();
        long notificationSeconds = notificationTime.toSecondOfDay();
        long diff = Math.abs(nowSeconds - notificationSeconds);

        return diff <= 60; // Отправляем если разница меньше 60 секунд
    }

    public void clearNotificationHistory(Long chatId) {
        lastNotificationSent.remove(chatId);
    }
}