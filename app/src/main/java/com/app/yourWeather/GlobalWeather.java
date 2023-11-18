package com.app.yourWeather;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GlobalWeather extends AppCompatActivity {


    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String UNITS_METRIC = "metric";
    private static final String UNITS_LANG = "ru";
    private static final String API_KEY = "064b58d59c738d8cff7324094ae5e0cd";

    private TextView locationTextView;
    private TextView temperatureTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_weather);

        locationTextView = findViewById(R.id.location);
        temperatureTextView = findViewById(R.id.temperature);

        // Проверка разрешений на местоположение
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Запрос разрешений, если они не были предоставлены
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // Разрешения уже есть, продолжаем с использованием местоположения
            getLocation();
        }
    }

    // Получение данных о местоположении
    private void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Вызов метода для загрузки погоды с использованием полученных координат
                WeatherHelper weatherHelper = new WeatherHelper();
                weatherHelper.loadWeatherData(latitude, longitude);

                // Здесь можно прекратить запросы на местоположение, если нужно
                locationManager.removeUpdates(this);
            }

            // Остальные методы LocationListener
        };

        // Проверка разрешений перед запросом на обновление местоположения
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Использование GPS_PROVIDER
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);

            // Добавление NETWORK_PROVIDER
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, locationListener);
        }
    }

    // Метод для обновления TextView с данными о местоположении
    public void updateLocationTextView(String location) {
        locationTextView.setText(location);

        Log.d("WeatherData", "Местоположение: " + location);
    }

    // Метод для обновления TextView с данными о температуре
    public void updateTemperatureTextView(double temperature) {
        String temperatureString = String.format(Locale.getDefault(), "%.0f°С", temperature);
        temperatureTextView.setText(temperatureString);

        Log.d("WeatherData", "Температура: " + temperatureString);
    }

    // Метод для обновления TextView с данными о ветре и направлении
    public void updateWindTextView(double windSpeed, double windDirection) {
        String direction = getWindDirection(windDirection);
        String windInfo = String.format(Locale.getDefault(), "%.1f м/с %s", windSpeed, direction);

        TextView windTextView = findViewById(R.id.windValue);
        windTextView.setText(windInfo);
    }


    // Метод для обновления TextView с данными о давлении
    public void updatePressureTextView(double pressure) {
        double pressureInMmHg = pressure * 0.75006375541921;
        TextView pressureTextView = findViewById(R.id.pressureValue);
        pressureTextView.setText(String.format("%.1f мм рт. ст.", pressureInMmHg));
    }

    // Метод для обновления TextView с данными о долготе
    public void updateLongitudeTextView(double longitude) {
        // Обновление TextView с данными о долготе
        TextView longitudeTextView = findViewById(R.id.longitude);
        longitudeTextView.setText(String.valueOf(longitude));
    }

    // Метод для обновления TextView с данными о широте
    public void updateLatitudeTextView(double latitude) {
        // Обновление TextView с данными о широте
        TextView latitudeTextView = findViewById(R.id.latitude);
        latitudeTextView.setText(String.valueOf(latitude));
    }

    public String getWindDirection(double degree) {
        if (degree >= 337.5 || degree < 22.5) {
            return "Северный";
        } else if (degree >= 22.5 && degree < 67.5) {
            return "Северо-восточный";
        } else if (degree >= 67.5 && degree < 112.5) {
            return "Восточный";
        } else if (degree >= 112.5 && degree < 157.5) {
            return "Юго-восточный";
        } else if (degree >= 157.5 && degree < 202.5) {
            return "Южный";
        } else if (degree >= 202.5 && degree < 247.5) {
            return "Юго-западный";
        } else if (degree >= 247.5 && degree < 292.5) {
            return "Западный";
        } else {
            return "Северо-западный";
        }
    }

    // Обработка результатов запроса разрешений
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение предоставлено, получаем местоположение
                getLocation();
            } else {
                // Разрешение не предоставлено, обработайте этот случай
            }
        }
    }

    // Класс для загрузки данных о погоде
    private class WeatherHelper {
        public void loadWeatherData(double latitude, double longitude) {

            long startTime = System.currentTimeMillis(); // Добавление переменной startTime

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            WeatherService service = retrofit.create(WeatherService.class);

            Call<WeatherResponse> call = service.getWeather(latitude, longitude, UNITS_METRIC, UNITS_LANG, API_KEY);
            call.enqueue(new Callback<WeatherResponse>() {
                @Override
                public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        WeatherResponse weatherResponse = response.body();
                        String city = weatherResponse.getCityName();
                        double temperature = weatherResponse.getMainWeatherInfo().getTemperature();
                        double windSpeed = weatherResponse.getWindInfo().getSpeed();
                        double windDirection = weatherResponse.getWindInfo().getDirection();
                        double pressure = weatherResponse.getMainWeatherInfo().getPressure();
                        double latitude = weatherResponse.getCoordinates().getLatitude();
                        double longitude = weatherResponse.getCoordinates().getLongitude();

                        long endTime = System.currentTimeMillis(); // Получение времени окончания запроса
                        long elapsedTime = endTime - startTime; // Вычисление времени запроса

                        // Добавление логов с данными и временем запроса
                        Log.d("WeatherData", "Получены данные для " + city + " за " + elapsedTime + " мс");

                        updateLocationTextView(city);
                        updateTemperatureTextView(temperature);
                        updateWindTextView(windSpeed, windDirection);
                        updatePressureTextView(pressure);
                        updateLatitudeTextView(latitude);
                        updateLongitudeTextView(longitude);
                    }
                }
                @Override
                public void onFailure(Call<WeatherResponse> call, Throwable t) {
                    // Обработка ошибки при выполнении запроса

                    Log.e("WeatherData", "Ошибка получения данных о погоде: " + t.getMessage());
                }
            });
        }
    }
}