package nz.ac.canterbury.seng302.gardenersgrove.component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

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

    public DailyWeather(String urlToWeatherIcon, LocalDate date, String description) {
        this.urlToWeatherIcon = urlToWeatherIcon;
        this.date = date;
        this.description = description;
    }


    public String getWeatherIcon() {
        return urlToWeatherIcon;
    }
    public String getDate() {
        if (date == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        return date.format(formatter);
    }
    public String getDay() {
        return (date == null) ? null : date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
    }
    public String getDescription() {
        return (description == null) ? null : description;
    }
    public String getMinTemp() {
        return (minTemp == null) ? null : minTemp.toString();
    }

    public void setMinTemp(Double minTemp) {
        this.minTemp = minTemp;
    }

    public String getMaxTemp() {
        return (maxTemp == null) ? null : maxTemp.toString();
    }

    public void setMaxTemp(Double maxTemp) {
        this.maxTemp = maxTemp;
    }

    public String getTemp() {
        return (currentTemp == null) ? null : currentTemp.toString();
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
