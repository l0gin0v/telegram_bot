package com.utils.services;

import com.utils.models.NominatimResponse;
import com.utils.models.Coordinates;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.lang.reflect.Type;

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

            assert response.body() != null;
            String responseBody = response.body().string();

            Type responseType = new TypeToken<List<NominatimResponse>>(){}.getType();
            List<NominatimResponse> results = gson.fromJson(responseBody, responseType);

            if (results.isEmpty()) {
                throw new IOException("Локация не найдена: " + locationName);
            }

            NominatimResponse firstResult = results.getFirst();

            return new Coordinates(firstResult);
        }
    }
}