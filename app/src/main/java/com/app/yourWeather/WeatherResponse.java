package com.app.yourWeather;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("name")
    private String cityName;

    @SerializedName("main")
    private MainWeatherInfo mainWeatherInfo;

    @SerializedName("wind")
    private WindInfo windInfo;

    @SerializedName("coord")
    private Coordinates coordinates;

    public String getCityName() {
        return cityName;
    }

    public MainWeatherInfo getMainWeatherInfo() {
        return mainWeatherInfo;
    }

    public WindInfo getWindInfo() {
        return windInfo;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }
}

class MainWeatherInfo {
    @SerializedName("temp")
    private double temperature;

    @SerializedName("pressure")
    private double pressure;

    public double getTemperature() {
        return temperature;
    }

    public double getPressure() {
        return pressure;
    }
}
class WeatherMainInfo {
    @SerializedName("temp")
    private double temperature;

    public double getTemperature() {
        return temperature;
    }
}

class WindInfo {
    @SerializedName("speed")
    private double speed;

    @SerializedName("deg")
    private double direction; // Добавим направление ветра

    public double getSpeed() {
        return speed;
    }

    public double getDirection() {
        return direction;
    }
}

class Coordinates {
    @SerializedName("lat")
    private double latitude;

    @SerializedName("lon")
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}