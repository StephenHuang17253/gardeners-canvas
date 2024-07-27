package nz.ac.canterbury.seng302.gardenersgrove.model;

import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;

/**
 * Model class for storing DailyWeather component
 * NOTE: attributes converted to string or int for Json conversion
 */
public class WeatherModel {
    String weatherIcon;
    String date;
    String description;
    String minTemp;
    String maxTemp;
    String currentTemp;
    String precipitation;
    Integer humidity;
    String error;
    String day;

    /**
     * WeatherModel Constructor for JSON construction
     */
    public WeatherModel(String weatherIcon, String date, String description,
                        String minTemp, String maxTemp, String currentTemp,
                        String precipitation, Integer humidity, String error, String day) {
        this.weatherIcon = weatherIcon;
        this.date = date;
        this.description = description;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.currentTemp = currentTemp;
        this.precipitation = precipitation;
        this.humidity = humidity;
        this.error = error;
        this.day = day;
    }

    /**
     * WeatherModel Constructor for controller construction
     */
    public WeatherModel(DailyWeather dailyWeather) {
        this.weatherIcon = dailyWeather.getWeatherIcon();
        this.date = dailyWeather.getDate();
        this.description = dailyWeather.getDescription();
        this.minTemp = dailyWeather.getMinTemp();
        this.maxTemp = dailyWeather.getMaxTemp();
        this.currentTemp = dailyWeather.getTemp();
        this.maxTemp = dailyWeather.getMaxTemp();
        this.precipitation = dailyWeather.getPrecipitation();
        this.error = dailyWeather.getWeatherError();
        this.day = dailyWeather.getDay();
    }



    public String getWeatherIcon() {
        return weatherIcon;
    }

    /**
     * Returns this date in format
     * @return formatted date in dd/MM
     */
    public String getDate() {
        return this.date;
    }

    /**
     * Returns this day
     * @return day in three letters such as SUN, MON, etc.
     */
    public String getDay() {
        return this.day;
    }
    public String getDescription() {
        return (description == null) ? null : description;
    }
    public String getMinTemp() {
        return minTemp;
    }



    public String getMaxTemp() {
        return this.maxTemp;
    }


    public String getTemp() {
        return currentTemp;
    }


    public String getPrecipitation() {
        return (precipitation == null) ? null : precipitation.toString();
    }

    public String getHumidity() {
        return (humidity == null) ? null : humidity.toString();
    }

    public String getWeatherError() {
        return (error == null) ? null : error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
