package com.example.weather.utils.shared_interfaces

import com.example.weather.utils.model.Local.DailyWeatherEntity

interface OnDayClickListener {
   fun onDayClick(item: DailyWeatherEntity)
}