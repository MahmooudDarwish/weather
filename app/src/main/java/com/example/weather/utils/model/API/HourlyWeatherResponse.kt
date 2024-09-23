package com.example.weather.utils.model.API

import com.example.weather.utils.model.ForecastItem

data class HourlyWeatherResponse(
    val cod: String,
    val message: Int,
    val cnt: Int,
    var list: List<ForecastItem>,
)



