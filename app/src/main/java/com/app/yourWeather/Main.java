package com.app.yourWeather;

import com.google.gson.annotations.SerializedName;

public class Main {
    @SerializedName("temp")
    private float temperature;

    public float getTemperature() {
        return temperature;
    }
}
