package com.utils.services;

import com.utils.models.Coordinates;
import com.utils.models.Notification;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

public class TelegramBot extends TelegramLongPollingBot {
    private final String botUsername;
    private final String botToken;
    private final WeatherAPI weatherAPI;
    private final WeatherBotDialogLogic weatherBotDialogLogic;
    private final Geocoding geocodingService;
    private final NotificationService notificationService;

    // Храним города пользователей
    private final Map<Long, String> userCities = new HashMap<>();
    // Храним состояния пользователей
    private final Map<Long, UserState> userStates = new HashMap<>();
    // Храним активные сессии
    private final Map<Long, Boolean> userSessions = new HashMap<>();

    private final Map<Long, LocalDate> lastNotificationSent = new ConcurrentHashMap<>();

    // Перечисление состояний пользователя
    private enum UserState {
        DEFAULT,           // Обычное состояние - обрабатываем команды
        WAITING_FOR_CITY,  // Ожидаем ввод города
        WAITING_FOR_NOTIFICATION_TIME, // Ожидаем времени уведомления
        INACTIVE           // Сессия завершена, ждем /start
    }

    public TelegramBot(String botUsername, String botToken) {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.weatherAPI = new WeatherAPI();
        this.weatherBotDialogLogic = new WeatherBotDialogLogic(weatherAPI);
        this.geocodingService = new Geocoding();

        WeatherFormatter weatherFormatter = new WeatherFormatter(weatherAPI);
        this.notificationService = new NotificationService(weatherAPI, weatherFormatter);

        Thread notificationThread = new Thread(() -> {
            while (true) {
                try {
                    checkAndSendNotifications();
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.err.println("Ошибка в notificationThread: " + e.getMessage());
                }
            }
        });

        notificationThread.setDaemon(true);
        notificationThread.start();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // Проверяем активна ли сессия пользователя
            if (!userSessions.getOrDefault(chatId, false) && !messageText.equals("/start")) {
                sendSessionInactiveMessage(chatId);
                return;
            }

            // Получаем текущее состояние пользователя
            UserState currentState = userStates.getOrDefault(chatId, UserState.DEFAULT);

            // Обрабатываем команды, которые работают в любом состоянии
            if (messageText.equals("/start")) {
                startUserSession(chatId);
                sendWelcomeMessage(chatId);
                return;
            }
            else if (messageText.equals("/help")) {
                sendHelp(chatId);
                return;
            }
            else if (messageText.equals("/quit")) {
                endUserSession(chatId);
                return;
            }

            // Если сессия не активна, игнорируем сообщения
            if (!userSessions.getOrDefault(chatId, false)) {
                return;
            }

            // Обрабатываем в зависимости от состояния
            switch (currentState) {
                case DEFAULT:
                    handleDefaultState(chatId, messageText);
                    break;
                case WAITING_FOR_CITY:
                    handleCityInputState(chatId, messageText);
                    break;
                case WAITING_FOR_NOTIFICATION_TIME:
                    handleNotificationTimeInput(chatId, messageText);
                    break;
                case INACTIVE:
                    sendSessionInactiveMessage(chatId);
                    break;
            }
        }
    }

    private void startUserSession(long chatId) {
        userSessions.put(chatId, true);
        userStates.put(chatId, UserState.DEFAULT);
    }

    private void endUserSession(long chatId) {
        String farewellText = "👋 До свидания! Сессия завершена.\nДля возобновления работы введите /start";
        // Отправляем сообщение с удалением клавиатуры
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(farewellText);

        // Удаляем клавиатуру
        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setRemoveKeyboard(true);
        message.setReplyMarkup(keyboardRemove);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        // Завершаем сессию
        userSessions.put(chatId, false);
        userStates.put(chatId, UserState.INACTIVE);

        notificationService.cancelNotification(chatId);
        // Очищаем данные пользователя (опционально)
        // userCities.remove(chatId);
    }

    private void checkAndSendNotifications() {
        try {
            // Получаем список активных уведомлений
            Set<Long> activeChats = notificationService.getActiveNotifications();

            for (Long chatId : activeChats) {
                // Проверяем активна ли сессия пользователя
                if (userSessions.getOrDefault(chatId, false)) {
                    // Получаем информацию об уведомлении
                    Notification notification = notificationService.getNotification(chatId);
                    if (notification == null) continue;

                    // Проверяем, не отправляли ли уже сегодня
                    LocalDate today = LocalDate.now();
                    LocalDate lastSent = lastNotificationSent.get(chatId);

                    if (lastSent != null && lastSent.equals(today)) {
                        continue; // Уже отправляли сегодня
                    }

                    // Проверяем время - пора ли отправлять?
                    LocalTime now = LocalTime.now();
                    LocalTime notificationTime = notification.getTime();

                    // Отправляем если текущее время +/- 1 минута от времени уведомления
                    if (isTimeToSend(now, notificationTime)) {
                        // Получаем текст уведомления
                        String notificationText = notificationService.getWeatherNotification(chatId);

                        if (notificationText != null && !notificationText.startsWith("❌")) {
                            // Отправляем уведомление
                            sendMessage(chatId, notificationText, KeyboardFactory.createMainWeatherKeyboard());
                            lastNotificationSent.put(chatId, today); // Запоминаем отправку
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
        // Разница в секундах
        long nowSeconds = now.toSecondOfDay();
        long notificationSeconds = notificationTime.toSecondOfDay();
        long diff = Math.abs(nowSeconds - notificationSeconds);

        // Отправляем если разница меньше 60 секунд (1 минута)
        return diff <= 60;
    }

    private void sendSessionInactiveMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(weatherBotDialogLogic.farewallWordsForInactive());

        ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
        keyboardRemove.setRemoveKeyboard(true);
        message.setReplyMarkup(keyboardRemove);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleDefaultState(long chatId, String messageText) {
        switch (messageText) {
            case "🌤 Сегодня":
                sendWeatherForPeriod(chatId, 1);
                break;
            case "📅 Завтра":
                sendWeatherForPeriod(chatId, 2);
                break;
            case "📆 3 дня":
                sendWeatherForPeriod(chatId, 3);
                break;
            case "🗓 Неделя":
                sendWeatherForPeriod(chatId, 7);
                break;
            case "📍 Сменить город":
                setUserState(chatId, UserState.WAITING_FOR_CITY);
                askForCity(chatId);
                break;
            case "🏙 Популярные города":
                setUserState(chatId, UserState.WAITING_FOR_CITY);
                showPopularCities(chatId);
                break;
            case "🔔 Уведомления":
                showNotificationMenu(chatId);
                break;
            case "⏰ Установить время":
                askForNotificationTime(chatId);
                break;
            case "ℹ️ Информация":
                String info = notificationService.getNotificationInfo(chatId);
                sendMessage(chatId, info, KeyboardFactory.createNotificationKeyboard());
                break;
            case "❌ Отменить":
                String result = notificationService.cancelNotification(chatId);
                sendMessage(chatId, result, KeyboardFactory.createMainWeatherKeyboard());
                break;
            case "↩️ Назад":
            case "↩️ Отмена":
                sendWelcomeMessage(chatId);
                break;
            default:
                // Если это не команда и не кнопка, игнорируем или показываем подсказку
                sendMessage(chatId,
                        "🤔 Используйте кнопки для навигации или введите /help для справки",
                        KeyboardFactory.createMainWeatherKeyboard()
                );
        }
    }

    private void showNotificationMenu(long chatId) {
        String city = userCities.get(chatId);

        if (city == null) {
            sendMessage(chatId,
                    "❌ Сначала выберите город для уведомлений",
                    KeyboardFactory.createMainWeatherKeyboard()
            );
            return;
        }

        String menuText = String.format(
                "🔔 Управление уведомлениями для %s:\n\n" +
                        "нажмите кнопку:",
                city
        );

        sendMessage(chatId, menuText, KeyboardFactory.createNotificationKeyboard());
    }

    private void handleNotificationTimeInput(long chatId, String timeInput) {
        // Проверяем, находимся ли мы в состоянии ожидания времени
        UserState currentState = userStates.getOrDefault(chatId, UserState.DEFAULT);

        if (!currentState.equals(UserState.WAITING_FOR_NOTIFICATION_TIME)) {
            // Если не в состоянии ожидания, игнорируем или показываем подсказку
            sendMessage(chatId,
                    "Нажмите ⏰ Установить время сначала",
                    KeyboardFactory.createMainWeatherKeyboard()
            );
            return;
        }

        if (timeInput.equals("↩️ Назад") || timeInput.equals("↩️ Отмена")) {
            setUserState(chatId, UserState.DEFAULT);
            showNotificationMenu(chatId);
            return;
        }

        if (!isValidTimeFormat(timeInput)) {
            sendMessage(chatId,
                    "❌ Неверный формат времени. Используйте HH:MM (например: 09:00)\n" +
                            "Попробуйте снова или нажмите ↩️ Отмена:",
                    KeyboardFactory.createCancelKeyboard()
            );
            return;
        }

        String city = userCities.get(chatId);
        if (city == null) {
            sendMessage(chatId,
                    "❌ Сначала выберите город",
                    KeyboardFactory.createMainWeatherKeyboard()
            );
            setUserState(chatId, UserState.DEFAULT);
            return;
        }

        try {
            String result = notificationService.setNotification(chatId, city, timeInput);
            sendMessage(chatId, result, KeyboardFactory.createMainWeatherKeyboard());
            setUserState(chatId, UserState.DEFAULT);

        } catch (Exception e) {
            sendMessage(chatId,
                    "❌ Ошибка: " + e.getMessage() + "\nПопробуйте снова:",
                    KeyboardFactory.createCancelKeyboard()
            );
        }
    }

    private void handleNotificationMenuInput(long chatId, String messageText) {
        switch (messageText) {
            case "⏰ Установить время":
                askForNotificationTime(chatId);
                break;
            case "ℹ️ Информация":
                String info = notificationService.getNotificationInfo(chatId);
                sendMessage(chatId, info, KeyboardFactory.createNotificationKeyboard());
                break;
            case "❌ Отменить":
                String result = notificationService.cancelNotification(chatId);
                sendMessage(chatId, result, KeyboardFactory.createMainWeatherKeyboard());
                break;
            case "↩️ Назад":
                sendWelcomeMessage(chatId);
                break;
            default:
                // Если это не кнопка, возможно пользователь ввел время напрямую
                // Проверяем формат времени HH:MM
                if (isValidTimeFormat(messageText)) {
                    handleNotificationTimeInput(chatId, messageText);
                } else {
                    sendMessage(chatId,
                            "🤔 Не понял команду. Используйте кнопки или введите время в формате HH:MM",
                            KeyboardFactory.createMainWeatherKeyboard()
                    );
                }
        }
    }

    private boolean isValidTimeFormat(String time) {
        return time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    }



    private void handleCityInputState(long chatId, String messageText) {
        if (messageText.equals("↩️ Назад") || messageText.equals("↩️ Отмена")) {
            setUserState(chatId, UserState.DEFAULT);
            sendWelcomeMessage(chatId);
            return;
        }

        try {
            Coordinates coords = geocodingService.getCoordinates(messageText);
            userCities.put(chatId, messageText);
            setUserState(chatId, UserState.DEFAULT);

            String confirmation = String.format(
                    "✅ Город установлен: %s\n\n" +
                            "Теперь вы можете:\n" +
                            "1. Посмотреть погоду (кнопки выше)\n" +
                            "2. Настроить уведомления (кнопка 🔔 Уведомления)",
                    coords.getDisplayName()
            );

            sendMessage(chatId, confirmation, KeyboardFactory.createMainWeatherKeyboard());

        } catch (Exception e) {
            sendMessage(chatId,
                    "❌ Не удалось найти город: " + messageText +
                            "\nПопробуйте уточнить название или нажмите ↩️ Отмена",
                    KeyboardFactory.createCancelKeyboard()
            );
        }
    }

    private void setUserState(long chatId, UserState state) {
        userStates.put(chatId, state);
    }

    private void sendWelcomeMessage(long chatId) {
        setUserState(chatId, UserState.DEFAULT);
        String userName = getUserName(chatId);
        String city = userCities.get(chatId);

        String text;
        if (city != null) {
            String notificationInfo = notificationService.getNotificationInfo(chatId);
            text = String.format(
                    "🌤 Привет, %s!\nДобро пожаловать в погодный бот!\n\n" +
                            "Ваш текущий город: %s\n\n" +
                            "%s\n\n" +
                            "Выберите действие:",
                    userName, city, notificationInfo
            );
        } else {
            text = String.format(
                    "🌤 Привет, %s!\nДобро пожаловать в погодный бот!\n\n" +
                            "Сначала выберите город, затем период прогноза.",
                    userName
            );
        }

        sendMessage(chatId, text, KeyboardFactory.createMainWeatherKeyboard());
    }

    private void askForCity(long chatId) {
        sendMessage(chatId,
                "🏙 Введите название города:\n(например: Москва, Санкт-Петербург, London)\n\n" +
                        "Или нажмите ↩️ Отмена для возврата",
                KeyboardFactory.createCancelKeyboard()
        );
    }

    private void askForNotificationTime(long chatId) {
        setUserState(chatId, UserState.WAITING_FOR_NOTIFICATION_TIME);
        sendMessage(chatId,
                "⏰ Введите время для уведомления (формат HH:MM):\n" +
                        "Например: 09:00, 18:30\n\n" +
                        "Бот будет присылать вам погоду каждый день в это время.\n\n" +
                        "Или нажмите ↩️ Отмена",
                KeyboardFactory.createCancelKeyboard()
        );
    }

    private void showPopularCities(long chatId) {
        sendMessage(chatId,
                "Выберите город из списка или введите свой:\n\n" +
                        "Или нажмите ↩️ Отмена для возврата",
                KeyboardFactory.createCitiesKeyboard()
        );
    }

    private void sendHelp(long chatId) {
        String helpText = weatherBotDialogLogic.getHelp();

        sendMessage(chatId, helpText, KeyboardFactory.createMainWeatherKeyboard());
        setUserState(chatId, UserState.DEFAULT);
    }

    private void sendWeatherForPeriod(long chatId, int days) {
        String city = userCities.get(chatId);

        if (city == null) {
            sendMessage(chatId,
                    "❌ Сначала выберите город с помощью кнопки \"📍 Сменить город\"",
                    KeyboardFactory.createMainWeatherKeyboard()
            );
            return;
        }

        try {
            String weatherText = weatherBotDialogLogic.getWeatherForPeriod(city, days);
            sendMessage(chatId, weatherText, KeyboardFactory.createMainWeatherKeyboard());

        } catch (Exception e) {
            sendMessage(chatId,
                    "❌ Ошибка при получении погоды для: " + city +
                            "\nПопробуйте выбрать другой город",
                    KeyboardFactory.createMainWeatherKeyboard()
            );
            e.printStackTrace();
        }
    }

    private String getUserName(long chatId) {
        return "друг";
    }

    private void sendMessage(long chatId, String text, ReplyKeyboardMarkup keyboard) {
        // Проверяем активна ли сессия
        if (!userSessions.getOrDefault(chatId, false)) {
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        if (keyboard != null) {
            message.setReplyMarkup(keyboard);
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}