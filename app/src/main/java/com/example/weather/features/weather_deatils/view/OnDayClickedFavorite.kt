package com.example.weather.features.weather_deatils.view

import com.example.weather.utils.model.Local.DailyWeatherEntity

interface OnDayClickedFavorite {

    fun onDayClicked(day: DailyWeatherEntity)
}