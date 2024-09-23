package com.example.weather.utils.model

data class Coordinates(
    val lon: Double,
    val lat: Double
)

data class Weather(
    val description: String,
    val icon: String
)

data class Main(
    val temp: Double,
    val pressure: Int,
    val humidity: Int,
)

data class Wind(
    val speed: Double,
)


data class Clouds(
    val all: Int
)

data class ForecastItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val dt_txt: String
)
