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

    static class MainWeatherInfo {
        @SerializedName("temp")
        private double temperature;

        @SerializedName("feels_like")
        private double feelsLikeTemperature; // Температура по ощущениям

        @SerializedName("pressure")
        private double pressure;

        @SerializedName("humidity")
        private double humidity;

        @SerializedName("temp_min")
        private double minTemperature;

        @SerializedName("temp_max")
        private double maxTemperature;

        public double getTemperature() {
            return temperature;
        }

        public double getFeelsLikeTemperature() {
            return feelsLikeTemperature;
        }

        public double getPressure() {
            return pressure;
        }

        public double getHumidity() {
            return humidity;
        }

        public double getMinTemperature() {
            return minTemperature;
        }

        public double getMaxTemperature() {
            return maxTemperature;
        }
    }

    static class WindInfo {
        @SerializedName("speed")
        private double speed;

        @SerializedName("deg")
        private double direction;

        public double getSpeed() {
            return speed;
        }

        public double getDirection() {
            return direction;
        }
    }

    static class Coordinates {
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
}
