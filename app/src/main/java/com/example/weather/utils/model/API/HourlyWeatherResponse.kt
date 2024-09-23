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


fun HourlyWeatherResponse.toHourlyWeatherEntities(): List<HourlyWeatherEntity> {
    return list.map { forecastItem ->
        forecastItem.toHourlyWeatherEntity(city.coord.lon, city.coord.lat)
    }
}

private fun ForecastItem.toHourlyWeatherEntity(longitude: Double, latitude: Double): HourlyWeatherEntity {
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
        dt_txt = dt_txt
    )
}


