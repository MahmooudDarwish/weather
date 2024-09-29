package com.example.weather.utils.model.Local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.weather.utils.model.Weather

@Entity(tableName = "daily_weather", primaryKeys = ["longitude", "latitude", "dt"])
data class DailyWeatherEntity(
    val longitude: Double,
    val latitude: Double,
    val dt: Long,
    val minTemp: Double,
    val maxTemp: Double,
    val windSpeed: Double,
    val pressure: Int,
    val humidity: Int,
    val description: String,
    val icon: String,
    val clouds: Int,
    val isFavorite: Boolean
)
