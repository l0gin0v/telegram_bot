package com.utils.services;

import com.utils.interfaces.INotificationClient;
import com.utils.interfaces.IConsole;
import com.utils.interfaces.IDialogLogic;
import com.utils.models.UserAnswerStatus;
import java.util.Scanner;

public class Console implements IConsole, INotificationClient {
    private final IDialogLogic dialogLogic;
    private final Scanner scanner;
    private final NotificationService notificationService;
    private final NotificationScheduler notificationScheduler;
    private boolean isRunning;
    private String currentCity;
    private static final long CONSOLE_USER_ID = 1L; // Уникальный ID для консольной сессии

    public Console(IDialogLogic dialogLogic) {
        this.dialogLogic = dialogLogic;
        this.scanner = new Scanner(System.in);
        this.isRunning = false;

        // Инициализация сервисов уведомлений
        WeatherAPI weatherAPI = new WeatherAPI();
        WeatherFormatter weatherFormatter = new WeatherFormatter(weatherAPI);
        this.notificationService = new NotificationService(weatherAPI, weatherFormatter);

        // Создаем планировщик с текущим экземпляром как NotificationClient
        this.notificationScheduler = new NotificationScheduler(notificationService, this);

        // Запуск планировщика уведомлений
        Thread notificationThread = new Thread(notificationScheduler);
        notificationThread.setDaemon(true);
        notificationThread.start();

        System.out.println("✅ NotificationScheduler запущен для консольного бота");
    }

    // Реализация методов интерфейса NotificationClient

    @Override
    public boolean isUserSessionActive(long userId) {
        // Для консоли проверяем только основной ID и активность сессии
        return isRunning && userId == CONSOLE_USER_ID;
    }

    @Override
    public void sendNotificationToUser(long userId, String notificationText) {
        if (userId == CONSOLE_USER_ID) {
            sendNotificationToConsole(notificationText);
        }
    }

    @Override
    public String getClientName() {
        return "ConsoleBot";
    }

    // Метод для отправки уведомлений в консоль
    private void sendNotificationToConsole(String notificationText) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🔔 ЕЖЕДНЕВНОЕ УВЕДОМЛЕНИЕ");
        System.out.println("=".repeat(50));
        System.out.println(notificationText);
        System.out.println("=".repeat(50) + "\n");
    }

    private void start() {
        isRunning = true;
        System.out.println(dialogLogic.welcomeWords());
        System.out.println("\nℹ️ Доступны ежедневные уведомления!");
        System.out.println("Для настройки введите 'уведомления' в главном меню");
    }

    @Override
    public void runBot() {
        System.out.println("Запуск консольного погодного бота...");
        System.out.println("Для начала работы введите /start");
        System.out.println("=".repeat(50));

        // Ожидаем команду /start
        while (!scanner.nextLine().trim().equalsIgnoreCase("/start")) {
            System.out.println(dialogLogic.needToStart());
        }

        start();

        // Основной цикл работы бота
        while (isRunning) {
            System.out.println("\n" + dialogLogic.getQuestion());
            System.out.print("(или введите 'уведомления' для настройки) ");

            boolean questionAnswered = false;

            while (!questionAnswered && isRunning) {
                System.out.print("\n>>> ");
                String userInput = scanner.nextLine().trim();

                // Обработка команд уведомлений
                if (userInput.equalsIgnoreCase("уведомления") ||
                        userInput.equalsIgnoreCase("notifications")) {
                    handleNotificationMenu();
                    continue;
                }

                // Обработка остальных команд
                UserAnswerStatus userAnswerStatus = dialogLogic.processAnswer(userInput);
                System.out.println("\n" + userAnswerStatus.message);

                // Если установлен город, сохраняем его для уведомлений
                if (userAnswerStatus.isCorrectAnswer && currentCity == null) {
                    // Пытаемся извлечь город из ответа
                    extractCityFromResponse(userAnswerStatus.message);
                }

                questionAnswered = userAnswerStatus.isCorrectAnswer;
                isRunning = !userAnswerStatus.isQuit;
            }
        }

        scanner.close();
        System.out.println("\n👋 Бот завершил работу.");
    }

    private void extractCityFromResponse(String response) {
        // Простая логика извлечения города из сообщения
        if (response.contains("Город установлен: ")) {
            String[] parts = response.split("Город установлен: ");
            if (parts.length > 1) {
                String cityPart = parts[1].split("\n")[0].trim();
                currentCity = cityPart;
                System.out.println("\n✅ Город '" + currentCity + "' сохранен для уведомлений");

                // Предлагаем настроить уведомления
                System.out.println("Хотите настроить ежедневные уведомления? (да/нет)");
                System.out.print(">>> ");
                String answer = scanner.nextLine().trim().toLowerCase();

                if (answer.equals("да") || answer.equals("yes")) {
                    handleNotificationMenu();
                }
            }
        }
    }

    private void handleNotificationMenu() {
        if (currentCity == null) {
            System.out.println("\n❌ Сначала выберите город для уведомлений");
            return;
        }

        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("🔔 УПРАВЛЕНИЕ УВЕДОМЛЕНИЯМИ");
            System.out.println("=".repeat(50));
            System.out.println("Город: " + currentCity);
            System.out.println("Текущие настройки: " + getNotificationStatus());
            System.out.println("=".repeat(50));
            System.out.println("1 - Установить/изменить время уведомления");
            System.out.println("2 - Показать текущие настройки");
            System.out.println("3 - Отключить уведомления");
            System.out.println("4 - Проверить уведомление сейчас");
            System.out.println("5 - Назад в главное меню");
            System.out.print("Выберите действие (1-5): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    setNotificationTime();
                    break;
                case "2":
                    showNotificationInfo();
                    break;
                case "3":
                    cancelNotification();
                    break;
                case "4":
                    testNotification();
                    break;
                case "5":
                    System.out.println("\n↩️ Возврат в главное меню");
                    return;
                default:
                    System.out.println("❌ Неверный выбор");
            }
        }
    }

    private void setNotificationTime() {
        System.out.print("\n⏰ Введите время для уведомления (формат HH:MM): ");
        String timeInput = scanner.nextLine().trim();

        if (!isValidTimeFormat(timeInput)) {
            System.out.println("❌ Неверный формат времени. Используйте HH:MM (например: 09:00)");
            return;
        }

        try {
            // Настраиваем уведомление через NotificationService
            String result = notificationService.setNotification(CONSOLE_USER_ID, currentCity, timeInput);
            System.out.println("\n" + result);

            // Очищаем историю отправленных уведомлений
            notificationScheduler.clearNotificationHistory(CONSOLE_USER_ID);

            System.out.println("\n✅ Уведомления настроены!");
            System.out.println("Город: " + currentCity);
            System.out.println("Время: " + timeInput);
            System.out.println("Бот будет присылать погоду ежедневно в это время.");

        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private void showNotificationInfo() {
        String info = notificationService.getNotificationInfo(CONSOLE_USER_ID);
        System.out.println("\n" + info);
    }

    private void cancelNotification() {
        String result = notificationService.cancelNotification(CONSOLE_USER_ID);
        System.out.println("\n" + result);

        // Очищаем историю в планировщике
        notificationScheduler.clearNotificationHistory(CONSOLE_USER_ID);
        currentCity = null; // Сбрасываем город

        System.out.println("✅ Все уведомления отключены");
    }

    private void testNotification() {
        if (currentCity == null) {
            System.out.println("❌ Сначала выберите город");
            return;
        }

        System.out.println("\n🔍 Тестирование уведомления...");
        try {
            String notificationText = notificationService.getWeatherNotification(CONSOLE_USER_ID);
            if (notificationText != null && !notificationText.startsWith("❌")) {
                sendNotificationToConsole("[ТЕСТ] " + notificationText);
                System.out.println("✅ Тестовое уведомление отправлено успешно!");
            } else {
                System.out.println("❌ Не удалось получить тестовое уведомление");
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка при тестировании: " + e.getMessage());
        }
    }

    private String getNotificationStatus() {
        // Проверяем, есть ли активное уведомление
        var notification = notificationService.getNotification(CONSOLE_USER_ID);
        if (notification == null) {
            return "отключены";
        } else {
            return "активны (" + notification.getTime() + ")";
        }
    }

    private boolean isValidTimeFormat(String time) {
        return time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    // Геттеры для тестирования
    public String getCurrentCity() {
        return currentCity;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }
}