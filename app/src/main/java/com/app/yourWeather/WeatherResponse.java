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

    @SerializedName("weather")
    private WeatherDetails[] weatherDetails;

    @SerializedName("visibility")
    private int visibility;

    @SerializedName("sys")
    private SystemInfo systemInfo;

    @SerializedName("rain")
    private RainInfo rainInfo;

    @SerializedName("snow")
    private SnowInfo snowInfo;

    @SerializedName("clouds")
    private Clouds clouds;

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

    public WeatherDetails[] getWeatherDetails() {
        return weatherDetails;
    }

    public int getVisibility() {
        return visibility;
    }

    public SystemInfo getSystemInfo() {
        return systemInfo;
    }

    public RainInfo getRainInfo() {
        return rainInfo;
    }

    public SnowInfo getSnowInfo() {
        return snowInfo;
    }

    public Clouds getClouds() {
        return clouds;
    }

    static class MainWeatherInfo {
        @SerializedName("temp")
        private double temperature;

        @SerializedName("feels_like")
        private double feelsLikeTemperature;

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

    static class WeatherDetails {
        @SerializedName("main")
        private String weatherMain;

        public String getWeatherMain() {
            return weatherMain;
        }

        public double getSnowVolume() {
            return 0;
        }

        public double getRainVolume() {
            return 0;
        }
    }

    static class SystemInfo {
        @SerializedName("sunrise")
        private long sunrise;

        @SerializedName("sunset")
        private long sunset;

        public long getSunrise() {
            return sunrise;
        }

        public long getSunset() {
            return sunset;
        }
    }

    static class RainInfo {
        @SerializedName("1h")
        private double rainVolume;

        public double getRainVolume() {
            return rainVolume;
        }
    }

    static class SnowInfo {
        @SerializedName("1h")
        private double snowVolume;

        public double getSnowVolume() {
            return snowVolume;
        }
    }

    static class Clouds {
        @SerializedName("all")
        private int cloudiness;

        public int getCloudiness() {
            return cloudiness;
        }
    }

    // Добавлены методы для получения объема дождя и снега
    public double getRainVolume() {
        if (rainInfo != null) {
            return rainInfo.getRainVolume();
        }
        return 0.0;
    }

    public double getSnowVolume() {
        if (snowInfo != null) {
            return snowInfo.getSnowVolume();
        }
        return 0.0;
    }
}