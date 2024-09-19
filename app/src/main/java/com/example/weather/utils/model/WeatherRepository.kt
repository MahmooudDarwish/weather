package com.example.weather.utils.model

import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

     fun fetchAndStoreWeatherData(longitude: Double, latitude: Double): Flow<WeatherResponse?>

}