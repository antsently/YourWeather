package com.app.yourWeather;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GlobalWeather extends AppCompatActivity implements LocationListener {

    private TextView locationTextView;
    private TextView temperatureTextView;
    private TextView weatherTypeTextView;
    private ImageView weatherImageView;
    private ProgressBar progressBar;

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String API_KEY = "064b58d59c738d8cff7324094ae5e0cd";

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_weather);

        locationTextView = findViewById(R.id.location);
        temperatureTextView = findViewById(R.id.temperature);
        weatherTypeTextView = findViewById(R.id.weatherType);
        weatherImageView = findViewById(R.id.weatherPicture);
        progressBar = findViewById(R.id.progressBar);

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
                showLocationPermissionError();
            }
        }
    }

    // Дополнительный метод для отображения сообщения об ошибке и кнопки "Включить GPS"
    private void showLocationPermissionError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ошибка доступа к местоположению");
        builder.setMessage("Для корректной работы приложения необходим доступ к местоположению. Пожалуйста, предоставьте разрешение в настройках приложения.");
        builder.setPositiveButton("Включить GPS", (dialog, which) -> {
            // Здесь можно добавить код для перехода в настройки приложения
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> {
            // Здесь можно добавить код для обработки отказа пользователя
        });
        builder.show();
    }

    private void getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Проверяем, есть ли у нас разрешение на доступ к местоположению
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Попытка получить текущее местоположение с использованием LocationListener
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

            // Если координаты не будут получены через LocationListener, вы можете использовать getLastKnownLocation
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                handleLocation(lastKnownLocation);
            } else {
                Log.e("WeatherDebug", "Last known location is null");
                // Если координаты не доступны, можно использовать значения по умолчанию
                getWeather(0.0, 0.0);
            }
        }
    }

    private void handleLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Log.d("WeatherDebug", "Latitude: " + latitude);
        Log.d("WeatherDebug", "Longitude: " + longitude);

        // Вызываем метод для получения погоды
        getWeather(latitude, longitude);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Получено новое местоположение через LocationListener
        handleLocation(location);

        // Прекращаем прослушивание местоположения, чтобы избежать постоянных обновлений
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(this);
    }

    // Другие методы интерфейса LocationListener, которые могут понадобиться вам
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }

    private void getWeather(double latitude, double longitude) {

        TextView latitudeTextView = findViewById(R.id.latitude);
        TextView longitudeTextView = findViewById(R.id.longitude);

        latitudeTextView.setText(String.format("%.6f", latitude));
        longitudeTextView.setText(String.format("%.6f", longitude));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService weatherService = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = weatherService.getWeather(latitude, longitude, API_KEY, "ru"); // Указываем язык "ru" для русского

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();

                    if (weatherResponse != null) {
                        locationTextView.setText(weatherResponse.getCityName());

                        // Преобразование температуры из Кельвинов в градусы Цельсия
                        float temperatureKelvin = weatherResponse.getMain().getTemperature();
                        float temperatureCelsius = temperatureKelvin - 273.15f;
                        temperatureTextView.setText(String.format("%.1f°C", temperatureCelsius));

                        String weatherType = setWeatherData(weatherResponse.getWeather()[0].getWeatherType());
                        weatherTypeTextView.setText(weatherType);

                        Log.d("WeatherDebug", "City: " + weatherResponse.getCityName());
                        Log.d("WeatherDebug", "Temperature: " + weatherResponse.getMain().getTemperature());
                        Log.d("WeatherDebug", "Weather Type: " + weatherResponse.getWeather()[0].getWeatherType());

                        // После успешного получения данных изменяем видимость элементов
                        findViewById(R.id.frameLayout).setVisibility(View.VISIBLE);
                    } else {
                        Log.e("WeatherError", "Failed to get weather data. Code: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                // Обработка ошибок
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private String setWeatherData(String weatherType) {
        // Маппинг типов погоды на соответствующие ресурсы изображений и текст
        Map<String, Pair<Integer, String>> weatherData = new HashMap<>();
        weatherData.put("Clear", new Pair<>(R.drawable.clear, "Ясно"));
        weatherData.put("Clouds", new Pair<>(R.drawable.cloud, "Облачно"));
        weatherData.put("Mist", new Pair<>(R.drawable.mist, "Туман"));
        weatherData.put("Rain", new Pair<>(R.drawable.rain, "Дождь"));
        weatherData.put("Snow", new Pair<>(R.drawable.snow, "Снег"));

        // Получение данных из маппинга
        Pair<Integer, String> data = weatherData.getOrDefault(weatherType, new Pair<>(R.drawable.clear, "Неопределенно"));
        int imageResource = data.first;
        String translatedWeatherType = data.second;

        // Установка изображения
        Glide.with(this).load(imageResource).into(weatherImageView);

        // Установка текста
        weatherTypeTextView.setText(translatedWeatherType);

        // Возвращаем строку с типом погоды
        return translatedWeatherType;
    }

}