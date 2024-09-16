package com.example.weather.utils.model

data class WeatherResponse(
    val main: Main,
    val wind: Wind,
    val weather: List<Weather>,
    val clouds: Clouds,
    val name: String,
    val dt: Long
)

data class Main(
    val temp: Double,
    val humidity: Int,
    val pressure: Int
)

data class Wind(
    val speed: Double
)

data class Weather(
    val description: String,
    val icon: String
)

data class Clouds(
    val all: Int
)
