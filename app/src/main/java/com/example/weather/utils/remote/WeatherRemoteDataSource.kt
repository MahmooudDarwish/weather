package com.example.weather.utils.remote

import com.example.weather.utils.model.ForecastResponse
import com.example.weather.utils.model.HourlyWeatherResponse
import com.example.weather.utils.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

interface  WeatherRemoteDataSource {
     fun getCurrentWeather(
        latitude: String, longitude: String, lang: String
    ): Flow<WeatherResponse?>

    fun getHourlyWeather(
        latitude: String, longitude: String, lang: String
    ):   Flow<HourlyWeatherResponse?>

     fun get5DayForecast(
        latitude: String,
        longitude: String,
        lang: String
    ) : Flow<ForecastResponse?>
}