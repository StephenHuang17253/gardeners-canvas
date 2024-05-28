package nz.ac.canterbury.seng302.gardenersgrove.component;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This entity class is a collection of weather data
 * Note: This is about past, current and future weather data
 */
public class WeatherResponseData {
    List<Integer> SUNNY_WEATHER_CODES = List.of(0, 1, 2);
    List<Integer> OVERCAST_WEATHER_CODES = List.of(3, 45, 48, 51);
    JsonNode current;
    JsonNode daily;
    JsonNode jsonTempMax;
    JsonNode jsonTempMin;
    JsonNode jsonDailyWeatherCodes;
    JsonNode jsonDailyPrecipitationSum;
    JsonNode jsonDates;
    DailyWeather currentWeather;
    List<DailyWeather> forecastWeather;
    List<DailyWeather> pastWeather;



    public WeatherResponseData(JsonNode jsonWeatherData) {
        current = jsonWeatherData.get("current");
        daily = jsonWeatherData.get("daily");
        setDailyWeather();
        setCurrentWeather();
        setForecastWeather();
        setPastWeather();
    }

    public DailyWeather getCurrentWeather() {
        return currentWeather;
    }

    public List<DailyWeather> getForecastWeather() {
        return forecastWeather;
    }

    public List<DailyWeather> getPastWeather() {
        return pastWeather;
    }

    /**
     * Creates a DailyWeather object containing all details of current weather.
     */
    void setCurrentWeather() {
        int weatherCode = current.get("weather_code").asInt();
        List<String> weatherDescriptionAndIcon = getWeatherDescriptionAndIcon(weatherCode);
        LocalDate currentTime = LocalDate.now();
        this.currentWeather = new DailyWeather(weatherDescriptionAndIcon.get(1), currentTime, weatherDescriptionAndIcon.get(0));
        currentWeather.setCurrentTemp(current.get("temperature_2m").asDouble());
        currentWeather.setHumidity(current.get("relative_humidity_2m").asInt());
        currentWeather.setPrecipitation(current.get("precipitation").asDouble());
    }

    /**
     * This method separates the API response for daily weather into specific JsonNodes
     */
    void setDailyWeather() {
        jsonTempMax =  daily.get("temperature_2m_max");
        jsonTempMin = daily.get("temperature_2m_min");
        jsonDailyWeatherCodes = daily.get("weather_code");
        jsonDailyPrecipitationSum = daily.get("precipitation_sum");
        jsonDates = daily.get("time");
    }

    /**
     * This method collects the API response for the past 2 days and adds them to the pastWeather
     */
    void setPastWeather() {
        List<DailyWeather> pastWeather = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            pastWeather.add(getWeatherDay(i));
        }
        this.pastWeather = pastWeather;
    }

    /**
     * This method collects the API response for the next 3 days and adds them to forecastWeather
     */
    void setForecastWeather() {
        List<DailyWeather> forecastWeather = new ArrayList<>();
        for (int i = 3; i < jsonDates.size()-1; i++) {
            forecastWeather.add(getWeatherDay(i));
        }
        this.forecastWeather = forecastWeather;
    }

    /**
     * This helper method creates a day with all weather details entered
     * @param i - the index of json weather details
     * @return the day with all weather details set
     */
    DailyWeather getWeatherDay(int i) {
        int weatherCode = jsonDailyWeatherCodes.get(i).asInt();
        List<String> weatherDescriptionAndIcon = getWeatherDescriptionAndIcon(weatherCode);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        formatter = formatter.withLocale(Locale.getDefault());
        LocalDate date = LocalDate.parse(jsonDates.get(i).asText(), formatter);
        DailyWeather day = new DailyWeather(weatherDescriptionAndIcon.get(1), date, weatherDescriptionAndIcon.get(0));
        day.setMaxTemp(jsonTempMax.get(i).asDouble());
        day.setMinTemp(jsonTempMin.get(i).asDouble());
        day.setPrecipitation(jsonDailyPrecipitationSum.get(i).asDouble());
        return day;
    }

    /**
     * This helper methods identifies the weather description and icon based on weather code
     * @param weatherCode integer that is part of WMO Weather interpretation codes (WW)
     * @return list of weather description and the name of icon file
     */
    public List<String> getWeatherDescriptionAndIcon(int weatherCode) {
        String weatherDescription;
        String iconFileName;
        if (SUNNY_WEATHER_CODES.contains(weatherCode)) {
            weatherDescription = "Sunny";
            iconFileName = "sunny.png";
        }  else if (OVERCAST_WEATHER_CODES.contains(weatherCode)) {
            weatherDescription = "Overcast";
            iconFileName = "overcast.png";
        } else {
            weatherDescription = "Rainy";
            iconFileName = "rainy.png";
        }
        return List.of(weatherDescription, iconFileName);
    }


}
