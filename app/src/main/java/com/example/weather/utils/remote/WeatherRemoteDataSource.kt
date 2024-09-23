package com.example.weather.utils.remote

import com.example.weather.utils.model.API.DailyWeatherResponse
import com.example.weather.utils.model.API.HourlyWeatherResponse
import com.example.weather.utils.model.API.WeatherResponse
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
    ) : Flow<DailyWeatherResponse?>
}