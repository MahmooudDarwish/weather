package com.example.weather.features.home.view

import com.example.weather.utils.model.API.DailyForecastItem

interface OnDayClickListener {
   fun onDayClick(item: DailyForecastItem)
}