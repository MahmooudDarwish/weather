package com.example.weather.utils.model

data class HourlyWeatherResponse(
    val cod: String,
    val message: Int,
    val cnt: Int,
    var list: List<ForecastItem>,
    val city: City
)



