package com.example.weather.features.alarm.view

import com.example.weather.utils.model.Local.AlarmEntity

interface OnDeleteClicked {
   fun  deleteClicked(alarm: AlarmEntity)
}