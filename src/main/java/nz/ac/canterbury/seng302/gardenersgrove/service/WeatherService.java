package nz.ac.canterbury.seng302.gardenersgrove.service;


import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {

    Logger logger = LoggerFactory.getLogger(WeatherService.class);

    JSONObject current;
    double currentTemp;
    int currentHumidity;
    int currentWeather;

    JSONObject hourly;


    public void getWeather(String gardenLatitude, String gardenLongitude) {
        String url = "https://api.open-meteo.com/v1/forecast?latitude="
                + gardenLatitude
                + "&longitude=" + gardenLongitude
                + "&current=temperature_2m,relative_humidity_2m,weather_code&hourly=temperature_2m,relative_humidity_2m,weather_code&past_days=2";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonObject = new JSONObject(response.body());

            current = jsonObject.getJSONObject("current");
            currentTemp = current.getDouble("temperature_2m");
            currentHumidity = current.getInt("relative_humidity_2m");
            currentWeather = current.getInt("weather_code");


            hourly = jsonObject.getJSONObject("hourly");

            logger.info(String.valueOf(currentHumidity));

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }
}
