package com.example.weather.utils.remote

import com.example.weather.utils.constants.Keys
import com.example.weather.utils.model.ForecastResponse
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

    @GET("forecast")
    fun get5DayForecast(
        @Query("q") city: String,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String = Keys.WEATHER_API_KEY,
        ): Response<ForecastResponse>

    @GET("forecast")
    suspend fun getHourlyForecast(
        @Query("q") location: String,
        @Query("units") units: String,
        @Query("lang") lang: String,
        @Query("appid") apiKey: String = Keys.WEATHER_API_KEY,
    ): Response<WeatherResponse>
}