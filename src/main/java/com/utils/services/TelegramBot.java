package com.utils.services;

import com.utils.models.Coordinates;
import com.utils.models.OpenMeteoResponse;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

public class TelegramBot extends TelegramLongPollingBot {
    private final String botUsername;
    private final String botToken;
    private final WeatherAPI weatherAPI;
    private final Geocoding geocodingService;

    // Храним города пользователей
    private final Map<Long, String> userCities = new HashMap<>();
    // Храним состояния пользователей
    private final Map<Long, UserState> userStates = new HashMap<>();
    // Храним активные сессии
    private final Map<Long, Boolean> userSessions = new HashMap<>();

    // Перечисление состояний пользователя
    private enum UserState {
        DEFAULT,           // Обычное состояние - обрабатываем команды
        WAITING_FOR_CITY,  // Ожидаем ввод города
        INACTIVE           // Сессия завершена, ждем /start
    }

    public TelegramBot(String botUsername, String botToken) {
        this.botUsername = botUsername;
        this.botToken = botToken;
        this.weatherAPI = new WeatherAPI();
        this.geocodingService = new Geocoding();
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

        // Очищаем данные пользователя (опционально)
        userCities.remove(chatId);
    }

    private void sendSessionInactiveMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("❌ Сессия завершена. Введите /start для начала новой сессии.");

        // Удаляем клавиатуру
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
        // В обычном состоянии обрабатываем только кнопки
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

    private void handleCityInputState(long chatId, String messageText) {
        // В состоянии ожидания города обрабатываем ввод как город
        if (messageText.equals("↩️ Назад") || messageText.equals("↩️ Отмена")) {
            setUserState(chatId, UserState.DEFAULT);
            sendWelcomeMessage(chatId);
            return;
        }

        try {
            // Проверяем, что город существует через геокодирование
            Coordinates coords = geocodingService.getCoordinates(messageText);

            // Сохраняем город для пользователя
            userCities.put(chatId, messageText);
            setUserState(chatId, UserState.DEFAULT); // Возвращаем в обычное состояние

            String confirmation = String.format(
                    "✅ Город установлен: %s\n\nТеперь вы можете посмотреть погоду",
                    coords.getDisplayName()
            );

            sendMessage(chatId, confirmation, KeyboardFactory.createMainWeatherKeyboard());

        } catch (Exception e) {
            sendMessage(chatId,
                    "❌ Не удалось найти город: " + messageText +
                            "\nПопробуйте уточнить название (например: Москва, Россия) или нажмите ↩️ Отмена",
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
            text = String.format(
                    "🌤 Привет, %s!\nДобро пожаловать в погодный бот!\n\n" +
                            "Ваш текущий город: %s\n" +
                            "Выберите период прогноза:",
                    userName, city
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

    private void showPopularCities(long chatId) {
        sendMessage(chatId,
                "Выберите город из списка или введите свой:\n\n" +
                        "Или нажмите ↩️ Отмена для возврата",
                KeyboardFactory.createCitiesKeyboard()
        );
    }

    private void sendHelp(long chatId) {
        String helpText =
                "📖 Помощь по боту:\n\n" +
                        "🌤 Получить погоду:\n" +
                        "  - Нажмите кнопку с периодом (Сегодня, Завтра и т.д.)\n" +
                        "  - Бот покажет погоду для вашего текущего города\n\n" +
                        "📍 Сменить город:\n" +
                        "  - Нажмите \"📍 Сменить город\" или \"🏙 Популярные города\"\n" +
                        "  - Введите название города\n" +
                        "  - Бот запомнит ваш выбор\n\n" +
                        "🔄 Управление сессией:\n" +
                        "  - /start - начать сессию\n" +
                        "  - /quit - завершить сессию\n" +
                        "  - /help - показать справку\n\n" +
                        "❓ Если что-то не работает:\n" +
                        "  - Проверьте правильность написания города\n" +
                        "  - Используйте форматы: \"Москва\" или \"Moscow, Russia\"";

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
            String weatherText = getWeatherForPeriod(city, days);
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

    private String getWeatherForPeriod(String city, int days) throws Exception {
        switch (days) {
            case 1:
                return weatherAPI.getFormattedWeatherByCity(city, 1);
            case 2:
                return formatTomorrowWeather(city);
            case 3:
                return weatherAPI.getFormattedWeatherByCity(city, 3);
            case 7:
                return weatherAPI.getFormattedWeatherByCity(city, 7);
            default:
                return weatherAPI.getQuickWeather(city);
        }
    }

    private String formatTomorrowWeather(String city) throws Exception {
        OpenMeteoResponse response = weatherAPI.getWeatherByCity(city, 2);
        Coordinates coords = geocodingService.getCoordinates(city);

        StringBuilder weatherText = new StringBuilder();
        weatherText.append(String.format("📅 Погода в %s на завтра:\n\n", city));

        // Берем данные для второго дня (индекс 1)
        double tempMin = response.getDaily().getTemperature_2m_min().get(1);
        double tempMax = response.getDaily().getTemperature_2m_max().get(1);
        String condition = weatherAPI.getWeatherCondition(response.getDaily().getWeathercode().get(1));
        double windSpeed = response.getDaily().getWindspeed_10m_max().get(1);

        weatherText.append(String.format("🌡 Температура: %.0f°C...%.0f°C\n", tempMin, tempMax))
                .append(String.format("%s\n", condition))
                .append(String.format("💨 Ветер: %.0f км/ч", windSpeed));

        return weatherText.toString();
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