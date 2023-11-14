package com.app.yourWeather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GlobalWeather extends AppCompatActivity {

    private TextView locationTextView;
    private TextView temperatureTextView;
    private TextView weatherTypeTextView;
    private ImageView weatherImageView;

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String API_KEY = "064b58d59c738d8cff7324094ae5e0cd";

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    /*Какой-то ебучий код для проверки и небольшой дефолтной логики*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_weather);

        locationTextView = findViewById(R.id.location);
        temperatureTextView = findViewById(R.id.temperature);
        weatherTypeTextView = findViewById(R.id.weatherType);
        weatherImageView = findViewById(R.id.weatherPicture);

        // Проверяем, есть ли у нас разрешение на доступ к местоположению
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Если нет, то запрашиваем у пользователя разрешение
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            // Если разрешение уже предоставлено, выполняем необходимые действия
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение получено, выполняем необходимые действия
                getLocation();
            } else {
                // Разрешение не предоставлено, обрабатываем ситуацию
                // Можете показать пользователю сообщение о том, что без разрешения GPS приложение не сможет корректно работать
            }
        }
    }

    private void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Вызываем метод для получения погоды
                getWeather(latitude, longitude);
            } else {
                // Если координаты не доступны, можно использовать значения по умолчанию
                getWeather(0.0, 0.0);
            }
        }
    }

    private void getWeather(double latitude, double longitude) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService weatherService = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = weatherService.getWeather(latitude, longitude, API_KEY, "ru"); // Указываем язык "ru" для русского

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();

                    if (weatherResponse != null) {
                        locationTextView.setText(weatherResponse.getCityName());

                        // Преобразование температуры из Кельвинов в градусы Цельсия
                        float temperatureKelvin = weatherResponse.getMain().getTemperature();
                        float temperatureCelsius = temperatureKelvin - 273.15f;
                        temperatureTextView.setText(String.format("%.1f°C", temperatureCelsius));

                        String weatherType = translateWeatherType(weatherResponse.getWeather()[0].getWeatherType());
                        weatherTypeTextView.setText(weatherType);

                        Log.d("WeatherDebug", "City: " + weatherResponse.getCityName());
                        Log.d("WeatherDebug", "Temperature: " + weatherResponse.getMain().getTemperature());
                        Log.d("WeatherDebug", "Weather Type: " + weatherResponse.getWeather()[0].getWeatherType());

                    } else {
                        Log.e("WeatherError", "Failed to get weather data. Code: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                // Обработка ошибок
            }
        });
    }

    private String translateWeatherType(String weatherType) {
        String translatedWeatherType;
        switch (weatherType) {
            case "Clear":
                setWeatherImage("clear");
                translatedWeatherType = "Ясно";
                break;
            case "Clouds":
                setWeatherImage("cloud");
                translatedWeatherType = "Облачно";
                break;
            case "Mist":
                setWeatherImage("mist");
                translatedWeatherType = "Туман";
                break;
            case "Rain":
                setWeatherImage("rain");
                translatedWeatherType = "Дождь";
                break;
            case "Snow":
                setWeatherImage("snow");
                translatedWeatherType = "Снег";
                break;
            default:
                setWeatherImage("clear"); // По умолчанию
                translatedWeatherType = "Неопределенно";
                break;
        }
        return translatedWeatherType;
    }

    private void setWeatherImage(String weatherType) {
        int imageResource;

        switch (weatherType) {
            case "clear":
                imageResource = R.drawable.clear;
                break;
            case "cloud":
                imageResource = R.drawable.cloud;
                break;
            case "mist":
                imageResource = R.drawable.mist;
                break;
            case "rain":
                imageResource = R.drawable.rain;
                break;
            case "snow":
                imageResource = R.drawable.snow;
                break;
            default:
                imageResource = R.drawable.clear; // По умолчанию
                break;
        }

        Glide.with(this).load(imageResource).into(weatherImageView);
    }
}
