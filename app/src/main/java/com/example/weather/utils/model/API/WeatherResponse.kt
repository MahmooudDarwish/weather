package com.example.weather.utils.model.API

import com.example.weather.utils.model.Clouds
import com.example.weather.utils.model.Coordinates
import com.example.weather.utils.model.Local.WeatherEntity
import com.example.weather.utils.model.Main
import com.example.weather.utils.model.Weather
import com.example.weather.utils.model.Wind


data class WeatherResponse(
    val coord: Coordinates,
    val weather: List<Weather>,
    val main: Main,
    val wind: Wind,
    val clouds: Clouds,
    val dt: Int,
    val id: Int,
    val name: String,
)


fun WeatherResponse.toWeatherEntity(city: String): WeatherEntity {
    var cityName = name
    if(cityName.isEmpty()){
        cityName = city
    }
    return WeatherEntity(
        longitude = coord.lon,
        latitude = coord.lat,
        description = weather.firstOrNull()?.description ?: "",
        icon = weather.firstOrNull()?.icon ?: "",
        temp = main.temp,
        pressure = main.pressure,
        humidity = main.humidity,
        windSpeed = wind.speed,
        clouds = clouds.all,
        dt = dt,
        name = cityName
    )
}