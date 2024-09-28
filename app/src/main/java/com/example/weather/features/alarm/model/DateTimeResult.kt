package com.example.weather.features.alarm.model

import java.util.Date

data class DateTimeResult(
    val startDate: Date,
    val endDate: Date,
    val startDateTimeMillis: Long,
    val endDateTimeMillis: Long
)