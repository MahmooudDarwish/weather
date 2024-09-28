package com.example.weather.features.alarm.model

import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import java.util.Date

data class DateTimeResult(
    val startDate: Date,
    val endDate: Date,
    val startDateTimeMillis: Long,
    val endDateTimeMillis: Long
)