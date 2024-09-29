package com.example.weather.utils.model.API

import com.example.weather.utils.model.City
import com.example.weather.utils.model.ForecastItem
import com.example.weather.utils.model.Local.HourlyWeatherEntity

data class HourlyWeatherResponse(
    val cod: String,
    val message: Int,
    val cnt: Int,
    var list: List<ForecastItem>,
    val city: City
)
fun HourlyWeatherResponse.toHourlyWeatherEntities(lon: String, lat: String, isFavorite: Boolean): List<HourlyWeatherEntity> {
    return list.map { forecastItem ->
        forecastItem.toHourlyWeatherEntity(lon.toDouble(), lat.toDouble(), isFavorite)
    }
}
private fun ForecastItem.toHourlyWeatherEntity(longitude: Double, latitude: Double, isFavorite: Boolean): HourlyWeatherEntity {
    return HourlyWeatherEntity(
        longitude = longitude,
        latitude = latitude,
        dt = dt,
        temp = main.temp,
        pressure = main.pressure,
        humidity = main.humidity,
        windSpeed = wind.speed,
        description = weather.firstOrNull()?.description ?: "",
        icon = weather.firstOrNull()?.icon ?: "",
        clouds = clouds.all,
        dt_txt = dt_txt,
        isFavorite = isFavorite
    )
}


