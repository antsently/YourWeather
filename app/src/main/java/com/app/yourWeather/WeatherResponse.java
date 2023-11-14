package com.app.yourWeather;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("name")
    private String cityName;

    @SerializedName("main")
    private Main main;

    @SerializedName("weather")
    private Weather[] weather;

    public String getCityName() {
        return cityName;
    }

    public Main getMain() {
        return main;
    }

    public Weather[] getWeather() {
        return weather;
    }
}

