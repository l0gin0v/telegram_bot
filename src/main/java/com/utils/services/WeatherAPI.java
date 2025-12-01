package com.utils.services;

import com.utils.models.OpenMeteoResponse;
import com.utils.models.Coordinates;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WeatherAPI {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Geocoding Geocoding;
    private static final String API_URL = "https://api.open-meteo.com/v1/forecast";

    public WeatherAPI(Geocoding Geocoding) {
        this.Geocoding = Geocoding;
    }

    public WeatherAPI() {
        this.Geocoding = new Geocoding();
    }

    public OpenMeteoResponse getWeather(double lat, double lon, int days) throws IOException {
        String url = String.format(
                "%s?latitude=%.4f&longitude=%.4f&daily=temperature_2m_max,temperature_2m_min,weathercode,precipitation_probability_max,windspeed_10m_max&timezone=auto&forecast_days=%d",
                API_URL, lat, lon, Math.min(days, 7)
        );

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ошибка при запросе к API погоды: " + response.code());
            }

            String responseBody = response.body().string();
            return gson.fromJson(responseBody, OpenMeteoResponse.class);
        }
    }

    public OpenMeteoResponse getWeatherByCity(String cityName, int days) throws IOException {
        Coordinates coordinates = Geocoding.getCoordinates(cityName);

        return getWeather(coordinates.getLat(), coordinates.getLon(), days);
    }

    public String getFormattedWeatherByCity(String cityName, int days) throws IOException {
        Coordinates coordinates = Geocoding.getCoordinates(cityName);

        OpenMeteoResponse weather = getWeather(coordinates.getLat(), coordinates.getLon(), days);

        return formatWeatherResponse(weather, coordinates.getDisplayName(), days);
    }

    private String formatWeatherResponse(OpenMeteoResponse response, String location, int days) {
        StringBuilder weatherText = new StringBuilder();

        if (days == 1) {
            double tempMin = response.getDaily().getTemperature2mMin().get(0);
            double tempMax = response.getDaily().getTemperature2mMax().get(0);
            String condition = getWeatherCondition(response.getDaily().getWeathercode().get(0));

            weatherText.append(String.format("🌤 Погода в %s:\n\n", location))
                    .append(String.format("🌡 Температура: %.0f°C...%.0f°C\n", tempMin, tempMax))
                    .append(String.format("%s\n", condition))
                    .append(String.format("💨 Ветер: %.0f км/ч\n", response.getDaily().getWindspeed10mMax().get(0)));

            if (response.getDaily().getPrecipitationProbabilityMax() != null) {
                double precipitation = response.getDaily().getPrecipitationProbabilityMax().get(0);
                weatherText.append(String.format("☔️ Вероятность дождя: %.0f%%", precipitation));
            }

        } else {
            weatherText.append(String.format("📅 Погода в %s на %d дней:\n\n", location, days));

            for (int i = 0; i < Math.min(days, response.getDaily().getTime().size()); i++) {
                String dayName = formatDay(response.getDaily().getTime().get(i));
                double tempMin = response.getDaily().getTemperature2mMin().get(i);
                double tempMax = response.getDaily().getTemperature2mMax().get(i);
                String condition = getWeatherCondition(response.getDaily().getWeathercode().get(i));

                weatherText.append(String.format("%s: %.0f°C...%.0f°C, %s\n",
                        dayName, tempMin, tempMax, condition));
            }
        }

        return weatherText.toString();
    }

    public String getQuickWeather(String cityName) throws IOException {
        try {
            return getFormattedWeatherByCity(cityName, 1);
        } catch (IOException e) {
            return "❌ Не удалось получить погоду для: " + cityName +
                    "\nПроверьте название города и попробуйте снова.";
        }
    }

    public String getWeatherCondition(int weatherCode) {
        if (weatherCode == 0) return "☀️ Ясно";
        if (weatherCode == 1) return "🌤 Преимущественно ясно";
        if (weatherCode == 2) return "⛅️ Переменная облачность";
        if (weatherCode == 3) return "☁️ Пасмурно";
        if (weatherCode >= 45 && weatherCode <= 48) return "🌫 Туман";
        if (weatherCode >= 51 && weatherCode <= 55) return "🌦 Морось";
        if (weatherCode >= 56 && weatherCode <= 57) return "🌨 Ледяная морось";
        if (weatherCode >= 61 && weatherCode <= 65) return "🌧 Дождь";
        if (weatherCode >= 66 && weatherCode <= 67) return "🌨 Ледяной дождь";
        if (weatherCode >= 71 && weatherCode <= 77) return "❄️ Снег";
        if (weatherCode >= 80 && weatherCode <= 82) return "🌦 Ливень";
        if (weatherCode >= 85 && weatherCode <= 86) return "🌨 Снегопад";
        if (weatherCode >= 95 && weatherCode <= 99) return "⛈ Гроза";

        return "❓ Неизвестно";
    }

    public String formatDay(String dateString) {
        LocalDate date = LocalDate.parse(dateString);
        LocalDate today = LocalDate.now();

        if (date.equals(today)) return "Сегодня";
        if (date.equals(today.plusDays(1))) return "Завтра";
        if (date.equals(today.plusDays(2))) return "Послезавтра";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        return date.format(formatter);
    }
}