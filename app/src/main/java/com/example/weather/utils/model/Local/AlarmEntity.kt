package com.example.weather.utils.model.Local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val icon: String,
    val fromHour: Int,
    val fromMinute: Int,
    val toHour: Int,
    val toMinute: Int,
    val startDate : Long,
    val endDate : Long,
    val date: Long,
    val isAlarm: Boolean
)
