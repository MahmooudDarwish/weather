package com.example.weather.utils.remote

import com.example.weather.utils.constants.Keys
import com.example.weather.utils.model.API.DailyWeatherResponse
import com.example.weather.utils.model.API.HourlyWeatherResponse
import com.example.weather.utils.model.API.WeatherResponse
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
        ): Response<DailyWeatherResponse>

    @GET("forecast/climate")
    suspend fun get30DayForecast(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("lang") lang: String,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = Keys.WEATHER_API_KEY,
    ): Response<DailyWeatherResponse>

    @GET("forecast/hourly")
    suspend fun get5DayHourlyForecast(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("lang") lang: String,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = Keys.WEATHER_API_KEY,
    ): Response<HourlyWeatherResponse>
}


