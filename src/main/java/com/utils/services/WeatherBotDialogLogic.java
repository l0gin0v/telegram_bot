package com.utils.services;

import com.utils.interfaces.IDialogLogic;
import com.utils.models.UserAnswerStatus;

public class WeatherBotDialogLogic implements IDialogLogic {
    private final WeatherAPI weatherAPI;

    public WeatherBotDialogLogic(WeatherAPI weatherAPI) {
        this.weatherAPI = weatherAPI;
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

    public UserAnswerStatus processAnswer(String answer) {
        if (answer.equals("/help")) {
            return new UserAnswerStatus(false, getHelp(), false);
        }
        else if (answer.equals("/quit")) {
            return new UserAnswerStatus(false, farewellWords(), true);
        }
        else {
            try {
                String weather = weatherAPI.getQuickWeather(answer);
                return new UserAnswerStatus(true, weather, false);
            } catch (Exception e) {
                return new UserAnswerStatus(false,
                        "Не удалось получить погоду для города: " + answer +
                                "\nПопробуйте еще раз или введите /quit", false);
            }
        }
    }

    private String getHelp() {
        return "Это погодный бот. Введите название города, и я покажу вам погоду.\n" +
                "Доступные команды:\n" +
                "/help - показать эту справку\n" +
                "/quit - выйти из бота";
    }

    public String getWeatherForPeriod(String city, String period) {
        try {
            switch (period) {
                case "today":
                    return weatherAPI.getFormattedWeatherByCity(city, 1);
                case "tomorrow":
                    return weatherAPI.getFormattedWeatherByCity(city, 2);
                case "3days":
                    return weatherAPI.getFormattedWeatherByCity(city, 3);
                case "week":
                    return weatherAPI.getFormattedWeatherByCity(city, 7);
                default:
                    return weatherAPI.getQuickWeather(city);
            }
        } catch (Exception e) {
            return "❌ Ошибка при получении погоды: " + e.getMessage();
        }
    }
}
