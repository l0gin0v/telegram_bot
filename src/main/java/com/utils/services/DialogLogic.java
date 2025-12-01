package com.utils.services;

import com.utils.interfaces.IDialogLogic;
import com.utils.models.UserAnswerStatus;

public class DialogLogic implements IDialogLogic {
    private final WeatherAPI weatherAPI;
    private final WeatherFormatter weatherFormatter;
    private String currentCity;

    public DialogLogic(WeatherAPI weatherAPI) {
        this.weatherAPI = weatherAPI;
        this.weatherFormatter = new WeatherFormatter(weatherAPI);
    }

    public String getQuestion() {
        if (currentCity == null) {
            return "Введите название города для получения погоды:";
        } else {
            return String.format(
                    "Ваш текущий город: %s\nВыберите действие:\n" +
                            "1 - Погода сегодня\n" +
                            "2 - Погода завтра\n" +
                            "3 - Погода на 3 дня\n" +
                            "4 - Погода на неделю\n" +
                            "5 - Сменить город\n" +
                            "Введите номер:",
                    currentCity
            );
        }
    }

    public String needToStart() {
        return "Для запуска бота введите /start";
    }

    public String welcomeWords() {
        return "Добро пожаловать в погодный бот!\n" +
                "==========================\n" +
                "Вы можете ввести название города для получения погоды.\n" +
                "===========================\n";
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
        else if (currentCity == null) {
            try {
                String weather = weatherFormatter.getQuickWeather(answer);
                currentCity = answer;
                return new UserAnswerStatus(true,
                        "✅ Город установлен: " + answer + "\n\n" + weather +
                                "\n\nТеперь вы можете выбрать период прогноза:",
                        false);
            } catch (Exception e) {
                return new UserAnswerStatus(false,
                        "❌ Не удалось получить погоду для города: " + answer +
                                "\nПопробуйте еще раз", false);
            }
        }
        else {
            // Обработка выбора периода
            switch (answer) {
                case "1":
                    return getWeatherForPeriod(1);
                case "2":
                    return getWeatherForPeriod(2);
                case "3":
                    return getWeatherForPeriod(3);
                case "4":
                    return getWeatherForPeriod(7);
                case "5":
                    currentCity = null;
                    return new UserAnswerStatus(false,
                            "Введите новый город:", false);
                default:
                    return new UserAnswerStatus(false,
                            "❌ Неверный выбор. Введите число от 1 до 5", false);
            }
        }
    }

    private UserAnswerStatus getWeatherForPeriod(int days) {
        try {
            String weather;
            switch (days) {
                case 1:
                    weather = weatherFormatter.getQuickWeather(currentCity);
                    break;
                case 2:
                    weather = weatherFormatter.formatTomorrowWeather(currentCity);
                    break;
                case 3:
                    var responseFor3Days = weatherAPI.getWeatherByCity(currentCity, 3);
                    var coordsFor3Days = weatherAPI.getGeocoding().getCoordinates(currentCity);
                    weather = weatherFormatter.formatWeatherResponse(
                            responseFor3Days, coordsFor3Days.getDisplayName(), 3
                    );
                    break;
                case 7:
                    var responseFor7Days = weatherAPI.getWeatherByCity(currentCity, 3);
                    var coordsFor7Days = weatherAPI.getGeocoding().getCoordinates(currentCity);
                    weather = weatherFormatter.formatWeatherResponse(
                            responseFor7Days, coordsFor7Days.getDisplayName(), 3
                    );
                    break;
                default:
                    weather = weatherFormatter.getQuickWeather(currentCity);
            }
            return new UserAnswerStatus(true, weather, false);
        } catch (Exception e) {
            return new UserAnswerStatus(false,
                    "❌ Ошибка при получении погоды: " + e.getMessage(), false);
        }
    }

    private String getHelp() {
        return "Погодный бот - справка:\n\n" +
                "Как использовать:\n" +
                "1. Введите название города\n" +
                "2. Выберите период прогноза (1-4)\n" +
                "3. Для смены города введите 5\n" +
                "4. Для выхода введите /quit\n\n" +
                "Команды:\n" +
                "/help - показать эту справку\n" +
                "/quit - выйти из бота";
    }
}