package com.example.weather.utils.model

import com.example.weather.utils.model.Local.WeatherEntity
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity

data class WeatherData(
    val currentWeather: WeatherEntity?,
    val hourlyWeather: List<HourlyWeatherEntity>,
    val dailyWeather: List<DailyWeatherEntity>
)
