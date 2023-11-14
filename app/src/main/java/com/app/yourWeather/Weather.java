package com.app.yourWeather;

import com.google.gson.annotations.SerializedName;

public class Weather {
    @SerializedName("main")
    private String weatherType;

    public String getWeatherType() {
        return weatherType;
    }
}
