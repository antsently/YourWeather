package com.app.yourWeather;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("main")
    private Main main;

    public Main getMain() {
        return main;
    }

    public class Main {
        @SerializedName("temp")
        private double temp;

        @SerializedName("humidity")
        private double humidity;

        public double getTemp() {
            return temp;
        }

        public double getHumidity() {
            return humidity;
        }
    }
}
