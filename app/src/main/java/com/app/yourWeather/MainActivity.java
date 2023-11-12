package com.app.yourWeather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MainActivity extends AppCompatActivity {

    private EditText editTextCity;
    private TextView textViewWeather;
    private WeatherApi weatherApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextCity = findViewById(R.id.editTextCity);
        textViewWeather = findViewById(R.id.textViewWeather);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        weatherApi = retrofit.create(WeatherApi.class);

        Button buttonGetWeather = findViewById(R.id.buttonGetWeather);
        buttonGetWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = editTextCity.getText().toString();
                if (!TextUtils.isEmpty(city)) {
                    getWeather(city);
                } else {
                    Toast.makeText(MainActivity.this, "Введите город", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getWeather(String city) {
        String apiKey = "bb5513fbf37527fb5fbee4af5b78a8b7";

        Call<WeatherResponse> call = weatherApi.getWeather(city, apiKey);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    // Обработка данных о погоде

                } else {
                    Toast.makeText(MainActivity.this, "Ошибка запроса: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Ошибка запроса: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

