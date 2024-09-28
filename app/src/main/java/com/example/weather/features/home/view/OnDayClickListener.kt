package com.example.weather.features.home.view

import com.example.weather.utils.model.Local.DailyWeatherEntity

interface OnDayClickListener {
   fun onDayClick(item: DailyWeatherEntity)
}