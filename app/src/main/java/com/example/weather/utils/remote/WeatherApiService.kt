package com.example.weather.utils.remote

import com.example.weather.utils.constants.Keys
import com.example.weather.utils.model.ForecastResponse
import com.example.weather.utils.model.HourlyWeatherResponse
import com.example.weather.utils.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {


    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("lang") lang: String,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = Keys.WEATHER_API_KEY,
    ): Response<WeatherResponse>

    @GET("forecast/daily")
    suspend fun get5DayForecast(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("lang") lang: String,
        @Query("cnt") cnt: String = "5",
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = Keys.WEATHER_API_KEY,
        ): Response<ForecastResponse>

    @GET("forecast/hourly")
    suspend fun get5DayHourlyForecast(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("lang") lang: String,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = Keys.WEATHER_API_KEY,
    ): Response<HourlyWeatherResponse>
}


