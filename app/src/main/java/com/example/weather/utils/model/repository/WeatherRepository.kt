package com.example.weather.utils.model.repository

import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import com.example.weather.utils.model.API.WeatherResponse
import com.example.weather.utils.model.API.DailyWeatherResponse
import com.example.weather.utils.model.API.HourlyWeatherResponse
import com.example.weather.utils.model.Local.AlarmEntity
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

     /// API
     fun fetchWeatherData(longitude: Double, latitude: Double): Flow<WeatherResponse?>
     fun fetchHourlyWeatherData(longitude: Double, latitude: Double): Flow<HourlyWeatherResponse?>
     fun get5DayForecast(longitude: Double, latitude: Double): Flow<DailyWeatherResponse?>

     fun get30DayForecast(longitude: Double, latitude: Double): Flow<DailyWeatherResponse?>

     /// Database
     fun getWeather(lon: Double, lat: Double): Flow<WeatherEntity>
     fun getDailyWeather(lon: Double, lat: Double): Flow<List<DailyWeatherEntity>>
     fun getHourlyWeather(lon: Double, lat: Double): Flow<List<HourlyWeatherEntity>>
     fun getAllFavoriteWeather(): Flow<List<WeatherEntity>>

     // Methods for inserting weather data
     suspend fun insertWeather(weather: WeatherEntity)
     suspend fun insertDailyWeather(dailyWeather: List<DailyWeatherEntity>)
     suspend fun insertHourlyWeather(hourlyWeather: List<HourlyWeatherEntity>)

     // Methods for deleting weather data
     suspend fun deleteFavoriteWeather(lon: Double, lat: Double)
     suspend fun deleteFavoriteDailyWeather(lon: Double, lat: Double)
     suspend fun deleteFavoriteHourlyWeather(lon: Double, lat: Double)

     // Methods for handle alarms
     suspend fun insertAlarm(alarm: AlarmEntity)
     fun getAllAlarms(): Flow<List<AlarmEntity>>
     suspend fun deleteAlarm(id: Long)

     /// SharedPreferences
     fun setTemperatureUnit(unit: Temperature)
     fun getTemperatureUnit(): Temperature

     fun setWindSpeedUnit(unit: WindSpeed)
     fun getWindSpeedUnit(): WindSpeed

     fun setLocationStatus(locationStatus: LocationStatus)
     fun getLocationStatus(): LocationStatus

     fun setLanguage(language: Language)
     fun getLanguage(): Language

     fun setNotificationStatus(status: Boolean)
     fun getNotificationStatus(): Boolean

     fun saveCurrentLocation(latitude: Double, longitude: Double)
     fun getCurrentLocation(): Pair<Double, Double>?

     fun setFirstLaunchCompleted()

     fun isFirstLaunch(): Boolean

}
