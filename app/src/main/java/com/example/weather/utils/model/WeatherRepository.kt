package com.example.weather.utils.model


import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

     // API
     fun fetchAndStoreWeatherData(longitude: Double, latitude: Double): Flow<WeatherResponse?>
     fun fetchHourlyWeatherData(longitude: Double, latitude: Double): Flow<HourlyWeatherResponse?>
     fun get5DayForecast(longitude: Double, latitude: Double): Flow<ForecastResponse?>

     // SharedPreferences
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
