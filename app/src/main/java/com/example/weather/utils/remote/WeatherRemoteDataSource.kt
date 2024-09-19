package com.example.weather.utils.remote

import com.example.weather.utils.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

interface  WeatherRemoteDataSource {
     fun getCurrentWeather(
        latitude: String, longitude: String, metric: String, lang: String
    ): Flow<WeatherResponse?>
}