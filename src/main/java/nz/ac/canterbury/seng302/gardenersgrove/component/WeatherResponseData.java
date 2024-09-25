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
    private static final List<Integer> SUNNY_WEATHER_CODES = List.of(0, 1, 2);
    private static final List<Integer> OVERCAST_WEATHER_CODES = List.of(3, 45, 48, 51);
    private JsonNode current;
    private JsonNode daily;
    private JsonNode jsonTempMax;
    private JsonNode jsonTempMin;
    private JsonNode jsonDailyWeatherCodes;
    private JsonNode jsonDailyPrecipitationSum;
    private JsonNode jsonDates;
    private List<DailyWeather> retrievedWeatherData;
    private int timeZoneOffset;
    private String timezone;

    /**
     * Constructor for the weather reponse data class
     * Takes a response from the weather api as an input
     * 
     * @param jsonWeatherData the response data from a class to the weather api as a
     *                        json file
     */
    public WeatherResponseData(JsonNode jsonWeatherData) {
        current = jsonWeatherData.get("current");
        daily = jsonWeatherData.get("daily");
        String timeString = jsonWeatherData.get("utc_offset_seconds").asText();
        timeZoneOffset = Integer.parseInt(timeString);
        timezone = jsonWeatherData.get("timezone").asText();
        setDailyWeather();
        convertToWeatherObjects();
        improveCurrentWeatherForecast();
    }

    public List<DailyWeather> getRetrievedWeatherData() {
        return retrievedWeatherData;
    }

    public int getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public String getTimeZone() {
        return timezone;
    }

    /**
     * Creates a DailyWeather object containing all details of current weather.
     */
    void improveCurrentWeatherForecast() {
        int weatherCode = current.get("weather_code").asInt();
        List<String> weatherDescriptionAndIcon = getWeatherDescriptionAndIcon(weatherCode);

        DailyWeather currentWeather = retrievedWeatherData.get(2);
        currentWeather.setDescription(weatherDescriptionAndIcon.get(0));
        currentWeather.setWeatherIcon(weatherDescriptionAndIcon.get(1));
    }

    /**
     * This method separates the API response for daily weather into specific
     * JsonNodes
     */
    void setDailyWeather() {
        jsonTempMax = daily.get("temperature_2m_max");
        jsonTempMin = daily.get("temperature_2m_min");
        jsonDailyWeatherCodes = daily.get("weather_code");
        jsonDailyPrecipitationSum = daily.get("precipitation_sum");
        jsonDates = daily.get("time");
    }

    /**
     * This method collects all API responses and adds them to a list of weather
     * object
     */
    void convertToWeatherObjects() {
        List<DailyWeather> weatherObjects = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            weatherObjects.add(getWeatherDay(i));
        }
        this.retrievedWeatherData = weatherObjects;
    }

    /**
     * This helper method creates a day with all weather details entered
     * 
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
     * This helper methods identifies the weather description and icon based on
     * weather code
     * 
     * @param weatherCode integer that is part of WMO Weather interpretation codes
     *                    (WW)
     * @return list of weather description and the name of icon file
     */
    public List<String> getWeatherDescriptionAndIcon(int weatherCode) {
        String weatherDescription;
        String iconFileName;
        if (SUNNY_WEATHER_CODES.contains(weatherCode)) {
            weatherDescription = "Sunny";
            iconFileName = "sunny.png";
        } else if (OVERCAST_WEATHER_CODES.contains(weatherCode)) {
            weatherDescription = "Overcast";
            iconFileName = "overcast.png";
        } else {
            weatherDescription = "Rainy";
            iconFileName = "rainy.png";
        }
        return List.of(weatherDescription, iconFileName);
    }

}
