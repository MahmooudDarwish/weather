package com.example.weather.utils.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forecast")
data class ForecastEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val city: String,
    val dateTime: Long,
    val temperature: Double,
    val windSpeed: Double,
    val description: String,
    val icon: String
)
