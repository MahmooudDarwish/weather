package com.example.weather.features.alarm.model

import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView

data class DialogComponents(
    val dateFrom: LinearLayout,
    val dateTo: LinearLayout,
    val dateFromBtn: Button,
    val saveBtn: Button,
    val timeFromTxt: TextView,
    val dateFromTxt: TextView,
    val timeToTxt: TextView,
    val dateToTxt: TextView,
    val alarmTypeRadioGroup: RadioGroup
)
