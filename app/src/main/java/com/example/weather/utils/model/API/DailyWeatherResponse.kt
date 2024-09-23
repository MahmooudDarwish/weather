package com.example.weather.utils.model.API

import com.example.weather.utils.model.Weather

data class DailyWeatherResponse(
    val cod: String,
    val message: Double,
    val cnt: Int,
    val list: List<DailyForecastItem>,
)

data class DailyForecastItem(
    val dt: Long,
    val speed: Double,
    val pressure: Int,
    val humidity: Int,
    val weather: List<Weather>,
    val temp: Temperature,
    val clouds: Int,
)

data class Temperature(
   val min :Double,
   val max: Double,
)

