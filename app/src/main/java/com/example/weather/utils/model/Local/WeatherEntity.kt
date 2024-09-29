package com.example.weather.utils.model.Local

import androidx.room.Entity


@Entity(tableName = "current_weather", primaryKeys = ["longitude", "latitude"])
data class WeatherEntity(
    val longitude: Double,
    val latitude: Double,
    val description: String,
    val icon: String,
    val temp: Double,
    val pressure: Int,
    val humidity: Int,
    val windSpeed: Double,
    val clouds: Int,
    val dt: Int,
    val name: String,
    val isFavorite: Boolean
)




