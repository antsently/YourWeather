package com.app.yourWeather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GlobalWeather extends AppCompatActivity {

    private EditText cityInput;
    private Button searchButton;
    private ImageView weatherImage;
    private TextView temperature;
    private TextView humidity;
    private TextView windSpeed;

    private Retrofit retrofit;
    private WeatherService weatherService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_weather);

        cityInput = findViewById(R.id.city_input);
        searchButton = findViewById(R.id.search_button);
        weatherImage = findViewById(R.id.weather_image);
        temperature = findViewById(R.id.temperature);
        humidity = findViewById(R.id.humidity);

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherService = retrofit.create(WeatherService.class);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityInput.getText().toString().trim(); // Удаление пробелов в начале и конце строки
                if (!city.isEmpty()) { // Проверка, что строка не пуста после удаления пробелов
                    getWeatherData(city);
                } else {
                    Toast.makeText(GlobalWeather.this, "Введите название города", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getWeatherData(String city) {
        Call<WeatherResponse> call = weatherService.getWeather(city, "metric", "064b58d59c738d8cff7324094ae5e0cd");
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();
                    double temperatureValue = weatherResponse.getMain().getTemp();
                    double humidityValue = weatherResponse.getMain().getHumidity();

                    // Установка текстовых данных
                    temperature.setText(String.valueOf(temperatureValue));
                    humidity.setText(String.valueOf(humidityValue));

                    // Установка картинки в зависимости от погоды
                    setWeatherImage(temperatureValue, humidityValue);
                } else {
                    Toast.makeText(GlobalWeather.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(GlobalWeather.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setWeatherImage(double temperature, double humidity) {
        int imageResource;

        // Определение картинки в зависимости от погоды
        if (humidity >= 95) {
            imageResource = R.drawable.rain;
        } else if (humidity >= 85) {
            imageResource = R.drawable.cloud; // Возможен дождь, пасмурно
        } else if (temperature > 25 && humidity < 80) {
            imageResource = R.drawable.clear;
        } else if (temperature > 15 && humidity > 80) {
            imageResource = R.drawable.rain;
        } else if (temperature < 0) {
            imageResource = R.drawable.snow;
        } else {
            imageResource = R.drawable.cloud;
        }

        // Установка картинки в ImageView
        weatherImage.setImageResource(imageResource);
    }

}
