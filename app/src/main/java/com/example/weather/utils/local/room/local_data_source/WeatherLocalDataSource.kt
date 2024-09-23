package com.example.weather.utils.local.room.local_data_source

import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import kotlinx.coroutines.flow.Flow

interface WeatherLocalDataSource {

    // Methods for retrieving weather data
    fun getCurrentWeather(lon: Double, lat: Double): Flow<WeatherEntity?>
    fun getDailyWeather(lon: Double, lat: Double): Flow<List<DailyWeatherEntity>>
    fun getHourlyWeather(lon: Double, lat: Double): Flow<List<HourlyWeatherEntity>>
    fun getAllFavoriteWeather(): Flow<List<WeatherEntity>>

    // Methods for inserting weather data
    suspend fun insertCurrentWeather(weather: WeatherEntity)
    suspend fun insertDailyWeather(dailyWeather: List<DailyWeatherEntity>)
    suspend fun insertHourlyWeather(hourlyWeather: List<HourlyWeatherEntity>)

    // Methods for deleting weather data
    suspend fun deleteCurrentWeather(lon: Double, lat: Double)
    suspend fun deleteDailyWeather(lon: Double, lat: Double)
    suspend fun deleteHourlyWeather(lon: Double, lat: Double)
}
