package com.utils.services;

import com.utils.models.Coordinates;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.net.URLEncoder;

public class Geocoding {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    public Coordinates getCoordinates(String locationName) throws IOException {
        String url = String.format(
                "%s?q=%s&format=json&limit=1",
                NOMINATIM_URL,
                URLEncoder.encode(locationName, "UTF-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "WeatherBot/1.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ошибка геокодирования: " + response.code());
            }

            String responseBody = response.body().string();
            JsonArray results = gson.fromJson(responseBody, JsonArray.class);

            if (results.size() == 0) {
                throw new IOException("Локация не найдена: " + locationName);
            }

            JsonObject firstResult = results.get(0).getAsJsonObject();
            double lat = firstResult.get("lat").getAsDouble();
            double lon = firstResult.get("lon").getAsDouble();
            String displayName = firstResult.get("display_name").getAsString();

            return new Coordinates(lat, lon, displayName);
        }
    }
}