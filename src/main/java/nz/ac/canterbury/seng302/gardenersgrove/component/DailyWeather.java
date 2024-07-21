package nz.ac.canterbury.seng302.gardenersgrove.component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Object storing key details about a day's weather
 * Contains date, weather description, weather icon name, temperatures, precipitation, and any errors that may appear.
 */
public class DailyWeather {
    String urlToWeatherIcon;
    LocalDate date;
    String description;
    Double minTemp;
    Double maxTemp;
    Double currentTemp;
    Double precipitation;
    Integer humidity;
    String error;

    /**
     * Constructor for class containing weather details of a day
     * @param urlToWeatherIcon string name of icon file
     * @param date LocalDate of this day
     * @param description string description of current weather such as Sunny or Rainy
     */
    public DailyWeather(String urlToWeatherIcon, LocalDate date, String description) {
        this.urlToWeatherIcon = urlToWeatherIcon;
        this.date = date;
        this.description = description;
    }


    public String getWeatherIcon() {
        return urlToWeatherIcon;
    }

    /**
     * Returns this date in format
     * @return formatted date in dd/MM
     */
    public String getDate() {
        if (date == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d");
        return date.format(formatter);
    }

    /**
     * Returns this day
     * @return day in three letters such as SUN, MON, etc.
     */
    public String getDay() {
        return (date == null) ? null : date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
    }
    public String getDescription() {
        return (description == null) ? null : description;
    }
    public String getMinTemp() {
        return (minTemp == null) ? null : String.valueOf(Math.round(minTemp));
    }

    public void setMinTemp(Double minTemp) {
        this.minTemp = minTemp;
    }

    public String getMaxTemp() {
        return (maxTemp == null) ? null : String.valueOf(Math.round(maxTemp));
    }

    public void setMaxTemp(Double maxTemp) {
        this.maxTemp = maxTemp;
    }

    public String getTemp() {
        return (currentTemp == null) ? null : String.valueOf(Math.round(currentTemp));
    }

    public void setCurrentTemp(Double currentTemp) {
        this.currentTemp = currentTemp;
    }

    public String getPrecipitation() {
        return (precipitation == null) ? null : precipitation.toString();
    }

    public void setPrecipitation(Double precipitation) {
        this.precipitation = precipitation;
    }

    public String getHumidity() {
        return (humidity == null) ? null : humidity.toString();
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public String getWeatherError() {
        return (error == null) ? null : error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
