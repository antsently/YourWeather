package com.app.yourWeather;

import java.util.List;

public class FiveDayForecastResponse {
    private List<WeatherData> list;

    public List<WeatherData> getList() {
        return list;
    }

    public static class WeatherData {
        private long timestamp;
        private MainWeatherInfo main;
        private WindInfo wind;

        public long getTimestamp() {
            return timestamp;
        }

        public MainWeatherInfo getMain() {
            return main;
        }

        public WindInfo getWind() {
            return wind;
        }
 }

    public static class MainWeatherInfo {
        private double temp;

        public double getTemp() {
            return temp;
        }
    }

    public static class WindInfo {
        private double speed;
        private double deg;

        public double getSpeed() {
            return speed;
        }

        public double getDeg() {
            return deg;
        }
    }
}
