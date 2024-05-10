package nz.ac.canterbury.seng302.gardenersgrove.service;


import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class WeatherService {

    Logger logger = LoggerFactory.getLogger(WeatherService.class);

    JSONObject current;
    JSONObject daily;

    JSONArray tempMax;
    JSONArray tempMin;
    JSONArray dailyWeatherCodes;
    JSONArray dailyRain;
    JSONArray dates;

    public void getWeather(String gardenLatitude, String gardenLongitude) {
        String url = "https://api.open-meteo.com/v1/forecast?latitude="
                + gardenLatitude
                + "&longitude=" + gardenLongitude
                + "&current=temperature_2m,relative_humidity_2m,weather_code&daily=weather_code,temperature_2m_max,temperature_2m_min,rain_sum&timezone=auto&past_days=2";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonObject = new JSONObject(response.body());

            current = jsonObject.getJSONObject("current");
            daily = jsonObject.getJSONObject("daily");

            getDailyWeather();
            getForecast();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public JSONObject getCurrentWeather() {
        return current;
    }

    public double getCurrentTemperature() {
        return current.getDouble("temperature_2m");
    }

    public double getCurrentHumidity() {
        return current.getDouble("relative_humidity_2m");
    }

    public int getCurrentWeatherCode() {
        return current.getInt("weather_code");
    }

    public JSONObject getDailyWeather() {
        tempMax =  daily.getJSONArray("temperature_2m_max");
        tempMin = daily.getJSONArray("temperature_2m_min");
        dailyWeatherCodes = daily.getJSONArray("weather_code");
        dailyRain = daily.getJSONArray("rain_sum");
        dates = daily.getJSONArray("time");

        return daily;
    }

    public JSONObject getPastWeather() {

        JSONObject pastWeather = new JSONObject();
        JSONArray pastTempMax = new JSONArray();
        JSONArray pastTempMin = new JSONArray();
        JSONArray pastWeatherCodes = new JSONArray();
        JSONArray pastRain = new JSONArray();
        JSONArray pastDates = new JSONArray();

       for (int i = 0; i < 2; i++) {
           pastTempMax.put(tempMax.get(i));
           pastTempMin.put(tempMin.get(i));
           pastWeatherCodes.put(dailyWeatherCodes.get(i));
           pastRain.put(dailyRain.get(i));
           pastDates.put(dates.get(i));

       }

       pastWeather.put("temperature_max", pastTempMax);
       pastWeather.put("temperature_min", pastTempMin);
       pastWeather.put("weather_code", pastWeatherCodes);
       pastWeather.put("rain", pastRain);
       pastWeather.put("date", pastDates);

       logger.info(pastWeather.toString());

       return pastWeather;
    }

    public JSONObject getForecast() {

        JSONObject forecastWeather = new JSONObject();
        JSONArray forecastTempMax = new JSONArray();
        JSONArray forecastTempMin = new JSONArray();
        JSONArray forecastWeatherCodes = new JSONArray();
        JSONArray forecastRain = new JSONArray();
        JSONArray forecastDates = new JSONArray();

        for (int i = 2; i < tempMax.length(); i++) {
            forecastTempMax.put(tempMax.get(i));
            forecastTempMin.put(tempMin.get(i));
            forecastWeatherCodes.put(dailyWeatherCodes.get(i));
            forecastRain.put(dailyRain.get(i));
            forecastDates.put(dates.get(i));

        }

        forecastWeather.put("temperature_max", forecastTempMax);
        forecastWeather.put("temperature_min", forecastTempMin);
        forecastWeather.put("weather_code", forecastWeatherCodes);
        forecastWeather.put("rain", forecastRain);
        forecastWeather.put("date", forecastDates);

        logger.info(forecastWeather.toString());

        return forecastWeather;
    }

}
