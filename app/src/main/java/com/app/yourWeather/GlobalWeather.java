package com.app.yourWeather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;

import java.text.SimpleDateFormat;
import java.util.Date;
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

    // Проверка включенности GPS
    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    // Проверка подключения к интернету
    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // Получение данных о местоположении
    private void getLocation() {
        if (!isGPSEnabled() || !isInternetConnected()) {
            // Если GPS или интернет отключены, показать пользователю сообщение
            showEnableLocationDialog();
            return;
        }

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
                // Обновление экрана с данными о погоде
            }
        };

        // Добавляем проверку разрешений перед запросом на обновление местоположения
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Использование GPS_PROVIDER
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
            // Добавление NETWORK_PROVIDER
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, locationListener);
        } else {
            // Разрешения не предоставлены, вывести сообщение пользователю
            Toast.makeText(this, "Приложению необходим доступ к местоположению для работы", Toast.LENGTH_SHORT).show();
        }
    }

    // Показать диалоговое окно с просьбой включить GPS и интернет
    private void showEnableLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Включите GPS и интернет")
                .setMessage("Для работы приложения необходимо включить GPS и подключиться к интернету.")
                .setPositiveButton("Настройки", (dialog, which) -> {
                    // Переход к настройкам устройства для включения GPS и интернета
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Отмена", (dialog, which) -> finish()) // Закрыть приложение при отмене
                .show();
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
                // Разрешение не предоставлено, проверяем, стоит ли показать объяснение
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Пользователь отказал в разрешении, но разрешения не были запрещены навсегда
                    // Здесь можно показать объяснение, почему разрешение необходимо для работы приложения
                    showPermissionExplanationDialog();
                } else {
                    // Пользователь отказал в разрешении и выбрал "Не спрашивать снова"
                    // Здесь можно показать сообщение о том, что разрешение не было предоставлено
                    Toast.makeText(this, "Приложению необходим доступ к местоположению для работы", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private BroadcastReceiver gpsSwitchStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                // Проверка включенности GPS после изменения состояния
                if (isGPSEnabled()) {
                    // Если GPS включен, обновить местоположение
                    getLocation();
                } else {
                    // Если GPS отключен, показать пользователю сообщение
                    showEnableLocationDialog();
                }
            }
        }
    };

    private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                // Проверка подключения к интернету после изменения состояния
                if (isInternetConnected()) {
                    // Если интернет подключен, обновить местоположение
                    getLocation();
                } else {
                    // Если интернет отключен, показать пользователю сообщение
                    showEnableInternetDialog();
                }
            }
        }
    };

    // Отображение диалога о включении интернета
    private void showEnableInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Подключение к интернету отсутствует")
                .setMessage("Для работы приложения необходимо подключение к интернету.")
                .setPositiveButton("Настройки", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Отмена", (dialog, which) -> finish())
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Регистрация приемников для отслеживания изменений состояния GPS и интернета
        registerReceiver(gpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Отмена регистрации приемников при приостановке активности
        unregisterReceiver(gpsSwitchStateReceiver);
        unregisterReceiver(networkChangeReceiver);
    }

    // Показать объяснение необходимости разрешения
    private void showPermissionExplanationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Разрешение на местоположение")
                .setMessage("Для работы приложения необходим доступ к местоположению.")
                .setPositiveButton("Предоставить разрешение", (dialog, which) -> {
                    // Повторный запрос разрешения после объяснения
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                })
                .setNegativeButton("Отмена", (dialog, which) -> finish()) // Закрыть приложение при отмене
                .show();
    }

    // Метод для обновления TextView с данными о местоположении
    public void updateLocationTextView(String location) {
        locationTextView.setText(location);
    }
    // Метод для обновления TextView с данными о температуре
    public void updateTemperatureTextView(double temperature) {
        String temperatureString = String.format(Locale.getDefault(), "%.0f°С", temperature);
        temperatureTextView.setText(temperatureString);
    }
    // Метод для обновления TextView с данными о влажности
    public void updateHumidityTextView(double humidity) {
        TextView humidityTextView = findViewById(R.id.humidityValue);
        humidityTextView.setText(String.format(Locale.getDefault(), "%.1f%%", humidity));
    }

    // Метод для обновления TextView с данными о температуре по ощущениям
    public void updateFeelsLikeTemperatureTextView(double feelsLikeTemperature) {
        TextView feelsLikeTemperatureTextView = findViewById(R.id.feelsLikeTemperatureValue);
        feelsLikeTemperatureTextView.setText(String.format(Locale.getDefault(), "%.0f°С", feelsLikeTemperature));
    }

    // Метод для обновления TextView с данными о максимальной температуре
    public void updateMaxTemperatureTextView(double maxTemperature) {
        TextView maxTemperatureTextView = findViewById(R.id.maxTemperatureValue);
        maxTemperatureTextView.setText(String.format(Locale.getDefault(), "%.0f°С", maxTemperature));
    }

    // Метод для обновления TextView с данными о минимальной температуре
    public void updateMinTemperatureTextView(double minTemperature) {
        TextView minTemperatureTextView = findViewById(R.id.minTemperatureValue);
        minTemperatureTextView.setText(String.format(Locale.getDefault(), "%.0f°С", minTemperature));
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

    // Метод для обновления TextView с данными о видимости
    public void updateVisibilityTextView(int visibility) {
        TextView visibilityTextView = findViewById(R.id.visibilityValue);
        visibilityTextView.setText(String.format(Locale.getDefault(), "%d м", visibility));
    }

    // Метод для обновления TextView с данными о осадках (дождь)
    public void updateRainTextView(double rainVolume) {
        TextView rainTextView = findViewById(R.id.rainValue);
        if (rainVolume > 0) {
            rainTextView.setText(String.format(Locale.getDefault(), "Дождь: %.2f мм", rainVolume));
        } else {
            rainTextView.setText("Без осадков");
        }
    }

    // Метод для обновления TextView с данными о осадках (снег)
    public void updateSnowTextView(double snowVolume) {
        TextView snowTextView = findViewById(R.id.snowValue);
        if (snowVolume > 0) {
            snowTextView.setText(String.format(Locale.getDefault(), "Снег: %.2f мм", snowVolume));
        } else {
            snowTextView.setText("Без осадков");
        }
    }
    // Метод для обновления TextView с данными о восходе/закате
    public void updateSunriseSunsetTextView(long sunrise, long sunset) {
        TextView sunriseSunsetTextView = findViewById(R.id.sunriseSunsetValue);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String sunriseString = sdf.format(new Date(sunrise * 1000));
        String sunsetString = sdf.format(new Date(sunset * 1000));
        sunriseSunsetTextView.setText("В: " + sunriseString + " З: " + sunsetString);
    }

    // Метод для обновления TextView с данными о облачности
    public void updateCloudinessTextView(int cloudinessPercentage) {
        TextView cloudinessTextView = findViewById(R.id.cloudinessValue);
        if (cloudinessPercentage == 0) {
            cloudinessTextView.setText("Ясно");
        } else if (cloudinessPercentage < 20) {
            cloudinessTextView.setText("Малооблачно");
        } else if (cloudinessPercentage < 70) {
            cloudinessTextView.setText("Перем. облачность");
        } else {
            cloudinessTextView.setText("Пасмурно");
        }
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
            return "С";
        } else if (degree >= 22.5 && degree < 67.5) {
            return "С-В";
        } else if (degree >= 67.5 && degree < 112.5) {
            return "В";
        } else if (degree >= 112.5 && degree < 157.5) {
            return "Ю-В";
        } else if (degree >= 157.5 && degree < 202.5) {
            return "Ю";
        } else if (degree >= 202.5 && degree < 247.5) {
            return "Ю-З";
        } else if (degree >= 247.5 && degree < 292.5) {
            return "З";
        } else {
            return "С-В";
        }
    }
    // Класс для загрузки данных о погоде
    private class WeatherHelper {
        private long lastRequestTime = 0;
        private int requestCount = 0;
        public void loadWeatherData(double latitude, double longitude) {
            long currentTime = System.currentTimeMillis();
            long elapsedTimeSinceLastRequest = currentTime - lastRequestTime;
            if (elapsedTimeSinceLastRequest >= 3600000) {
                requestCount = 0;
            }

            if (requestCount < 3) {
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
                            double humidity = weatherResponse.getMainWeatherInfo().getHumidity();
                            double feelsLikeTemperature = weatherResponse.getMainWeatherInfo().getFeelsLikeTemperature();
                            double maxTemperature = weatherResponse.getMainWeatherInfo().getMaxTemperature();
                            double minTemperature = weatherResponse.getMainWeatherInfo().getMinTemperature();
                            double windSpeed = weatherResponse.getWindInfo().getSpeed();
                            double windDirection = weatherResponse.getWindInfo().getDirection();
                            double pressure = weatherResponse.getMainWeatherInfo().getPressure();
                            double latitude = weatherResponse.getCoordinates().getLatitude();
                            double longitude = weatherResponse.getCoordinates().getLongitude();

                            long sunrise = weatherResponse.getSystemInfo().getSunrise();
                            long sunset = weatherResponse.getSystemInfo().getSunset();

                            int visibility = weatherResponse.getVisibility();
                            double rainVolume = checkRain(weatherResponse);
                            double snowVolume = checkSnow(weatherResponse);
                            int cloudinessPercentage = calculateCloudinessPercentage(weatherResponse);

                            long endTime = System.currentTimeMillis();
                            long elapsedTime = endTime - currentTime;

                            Log.d("WeatherData", "Город: " + city);
                            Log.d("WeatherData", "Температура: " + temperature);
                            Log.d("WeatherData", "Влажность: " + humidity);
                            Log.d("WeatherData", "Ощущается как: " + feelsLikeTemperature);
                            Log.d("WeatherData", "Максимальная температура: " + maxTemperature);
                            Log.d("WeatherData", "Минимальная температура: " + minTemperature);
                            Log.d("WeatherData", "Скорость ветра: " + windSpeed);
                            Log.d("WeatherData", "Направление ветра: " + windDirection);
                            Log.d("WeatherData", "Давление: " + pressure);
                            Log.d("WeatherData", "Широта: " + latitude);
                            Log.d("WeatherData", "Долгота: " + longitude);
                            Log.d("WeatherData", "Видимость: " + visibility);
                            Log.d("WeatherData", "Дождь: " + rainVolume + " мм");
                            Log.d("WeatherData", "Снег: " + snowVolume + " мм");
                            Log.d("WeatherData", "Облачность: " + cloudinessPercentage + "%");
                            Log.d("WeatherData", "Восход солнца: " + sunrise);
                            Log.d("WeatherData", "Закат солнца: " + sunset);
                            Log.d("WeatherData", "Получены данные для " + city + " за " + elapsedTime + " мс");

                            updateLocationTextView(city);
                            updateTemperatureTextView(temperature);
                            updateHumidityTextView(humidity);
                            updateFeelsLikeTemperatureTextView(feelsLikeTemperature);
                            updateMaxTemperatureTextView(maxTemperature);
                            updateMinTemperatureTextView(minTemperature);
                            updateWindTextView(windSpeed, windDirection);
                            updatePressureTextView(pressure);
                            updateLatitudeTextView(latitude);
                            updateLongitudeTextView(longitude);
                            updateVisibilityTextView(visibility);
                            updateRainTextView(rainVolume);
                            updateSnowTextView(snowVolume);
                            updateCloudinessTextView(cloudinessPercentage);
                            updateSunriseSunsetTextView(sunrise, sunset);

                            lastRequestTime = System.currentTimeMillis();
                            requestCount++;

                        }
                    }
                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        Log.e("WeatherData", "Ошибка получения данных о погоде: " + t.getMessage());
                    }
                });
            } else {
                Toast.makeText(GlobalWeather.this, "Достигнуто максимальное количество запросов к API за час. Попробуйте через час =)", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // Обновление методов проверки осадков и облачности
    private double checkRain(WeatherResponse weatherResponse) {
        if (weatherResponse.getRainInfo() != null) {
            return weatherResponse.getRainInfo().getRainVolume();
        }
        return 0.0;
    }

    private double checkSnow(WeatherResponse weatherResponse) {
        if (weatherResponse.getSnowInfo() != null) {
            return weatherResponse.getSnowInfo().getSnowVolume();
        }
        return 0.0;
    }

    private int calculateCloudinessPercentage(WeatherResponse weatherResponse) {
        WeatherResponse.Clouds clouds = weatherResponse.getClouds();
        if (clouds != null) {
            return clouds.getCloudiness();
        }
        return 0;
    }
}