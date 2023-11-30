package com.app.yourWeather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("weather")
    Call<WeatherResponse> getWeather(@Query("lat") double latitude, @Query("lon") double longitude, @Query("units") String units, @Query("lang") String lang, @Query("appid") String apiKey);
}