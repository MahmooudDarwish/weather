package com.example.weather.utils.remote

import com.example.weather.utils.model.WeatherResponse

interface  WeatherRemoteDataSource {
     fun getCurrentWeather(
        latitude: String, longitude: String, metric: String, lang: String
    ): WeatherResponse?
}