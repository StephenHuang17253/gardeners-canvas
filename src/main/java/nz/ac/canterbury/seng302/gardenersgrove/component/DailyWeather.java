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
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        return date.format(formatter);
    }
    public String getDay() {
        if (date == null) {
            return "";
        }
        return date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
    }
    public String getDescription() {
        if (description == null) {
            return "";
        }
        return description;
    }
    public String getMinTemp() {
        if (minTemp == null) {
            return "";
        }
        return minTemp.toString();
    }

    public void setMinTemp(Double minTemp) {
        this.minTemp = minTemp;
    }

    public String getMaxTemp() {
        if (maxTemp == null) {
            return "";
        }
        return maxTemp.toString();
    }

    public void setMaxTemp(Double maxTemp) {
        this.maxTemp = maxTemp;
    }

    public String getTemp() {
        if (currentTemp == null) {
            return "";
        }
        return currentTemp.toString();
    }

    public void setCurrentTemp(Double currentTemp) {
        this.currentTemp = currentTemp;
    }

    public String getPrecipitation() {
        if (precipitation == null) {
            return "";
        }
        return precipitation.toString();
    }

    public void setPrecipitation(Double precipitation) {
        this.precipitation = precipitation;
    }

    public String getHumidity() {
        if (humidity == null) {
            return "";
        }
        return humidity.toString();
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public String getWeatherError() {
        if (error == null) {
            return "";
        }
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
