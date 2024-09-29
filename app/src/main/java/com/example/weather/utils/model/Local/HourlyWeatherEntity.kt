package com.example.weather.utils.model.Local

import androidx.room.Entity


@Entity(tableName = "hourly_weather", primaryKeys = ["longitude", "latitude", "dt"])
data class HourlyWeatherEntity(
    val longitude: Double,
    val latitude: Double,
    val dt: Long,
    val temp: Double,
    val pressure: Int,
    val humidity: Int,
    val windSpeed: Double,
    val description: String,
    val icon: String,
    val clouds: Int,
    val dt_txt: String,
    val isFavorite: Boolean
)
