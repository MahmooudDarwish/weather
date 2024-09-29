package com.example.weather.utils.model.API

import com.example.weather.utils.model.City
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Weather

data class DailyWeatherResponse(
    val cod: String,
    val message: Double,
    val cnt: Int,
    val list: List<DailyForecastItem>,
    val city: City
)
data class DailyForecastItem(
    val dt: Long,
    val speed: Double,
    val pressure: Int,
    val humidity: Int,
    val weather: List<Weather>,
    val temp: Temperature,
    val clouds: Int,
)
data class Temperature(
   val min :Double,
   val max: Double,
)

fun DailyWeatherResponse.toDailyWeatherEntities( lon:String ,  lat:String,isFavourite: Boolean = false): List<DailyWeatherEntity> {
    return list.map { item ->
        DailyWeatherEntity(
            longitude = lon.toDouble(),
            latitude = lat.toDouble(),
            dt = item.dt,
            minTemp = item.temp.min,
            maxTemp = item.temp.max,
            windSpeed = item.speed,
            pressure = item.pressure,
            humidity = item.humidity,
            description = item.weather.firstOrNull()?.description ?: "",
            icon = item.weather.firstOrNull()?.icon ?: "",
            clouds = item.clouds,
            isFavorite = isFavourite
        )
    }
}
