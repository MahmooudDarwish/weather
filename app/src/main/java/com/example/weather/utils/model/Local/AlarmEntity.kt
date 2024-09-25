package com.example.weather.utils.model.Local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey val startDate : Long,
    val date: Long,
    val title: String,
    val description: String,
    val icon: String,
    val fromHour: Int,
    val fromMinute: Int,
    val toHour: Int,
    val toMinute: Int,
    val endDate : Long,
    val isAlarm: Boolean
)
