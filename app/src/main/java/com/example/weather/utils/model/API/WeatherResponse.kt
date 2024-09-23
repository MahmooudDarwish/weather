package com.example.weather.utils.model.API

import com.example.weather.utils.model.Clouds
import com.example.weather.utils.model.Coordinates
import com.example.weather.utils.model.Main
import com.example.weather.utils.model.Weather
import com.example.weather.utils.model.Wind


data class WeatherResponse(
    val coord: Coordinates,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val clouds: Clouds,
    val dt: Int,
    val id: Int,
    val name: String,
)


