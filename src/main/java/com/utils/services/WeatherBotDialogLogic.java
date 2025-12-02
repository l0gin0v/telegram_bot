package com.utils.services;

import com.utils.interfaces.IDialogLogic;
import com.utils.models.UserAnswerStatus;

public class WeatherBotDialogLogic implements IDialogLogic {
    private final WeatherAPI weatherAPI;
    private final WeatherFormatter weatherFormatter;

    public WeatherBotDialogLogic(WeatherAPI weatherAPI) {
        this.weatherAPI = weatherAPI;
        this.weatherFormatter = new WeatherFormatter(weatherAPI);
    }

    public String getQuestion() {
        return "Введите название города для получения погоды:";
    }

    public String needToStart() {
        return "Для запуска бота введите /start";
    }

    public String welcomeWords() {
        return "Добро пожаловать в погодный бот!\n" +
                "==========================\n" +
                "Доступные команды:\n" +
                "  /help - получить справку\n" +
                "  /quit - выйти из бота\n" +
                "===========================\n" +
                "Вы можете ввести название города в любой момент для получения погоды.";
    }

    private String farewellWords() {
        return "До свидания! Возвращайтесь еще!";
    }

    public String farewallWordsForInactive() {
        return "❌ Сессия завершена. Введите /start для начала новой сессии.";
    }

    public UserAnswerStatus processAnswer(String answer) {
        if (answer.equals("/help")) {
            return new UserAnswerStatus(false, getHelp(), false);
        }
        else if (answer.equals("/quit")) {
            return new UserAnswerStatus(false, farewellWords(), true);
        }
        else {
            try {
                String weather = weatherFormatter.getQuickWeather(answer);
                return new UserAnswerStatus(true, weather, false);
            } catch (Exception e) {
                return new UserAnswerStatus(false,
                        "Не удалось получить погоду для города: " + answer +
                                "\nПопробуйте еще раз или введите /quit", false);
            }
        }
    }

    public String getHelp() {
        return "📖 Помощь по боту:\n\n" +
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
    }

    public String getWeatherForPeriod(String city, int days) {
        try {
            switch (days) {
                case 1:
                    return weatherFormatter.getQuickWeather(city);
                case 2:
                    return weatherFormatter.formatTomorrowWeather(city);
                case 3:
                    var responseFor3Days = weatherAPI.getWeatherByCity(city, 3);
                    var coordsFor3Days = weatherAPI.getGeocoding().getCoordinates(city);
                    return weatherFormatter.formatWeatherResponse(
                            responseFor3Days, coordsFor3Days.getDisplayName(), 3
                    );
                case 7:
                    var responseFor7Days = weatherAPI.getWeatherByCity(city, 7);
                    var coordsFor7Days = weatherAPI.getGeocoding().getCoordinates(city);
                    return weatherFormatter.formatWeatherResponse(
                            responseFor7Days, coordsFor7Days.getDisplayName(), 7
                    );
                default:
                    return weatherFormatter.getQuickWeather(city);
            }
        } catch (Exception e) {
            return "❌ Ошибка при получении погоды: " + e.getMessage();
        }
    }
}
