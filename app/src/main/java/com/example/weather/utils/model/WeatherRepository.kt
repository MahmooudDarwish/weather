package com.example.weather.utils.model


import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.Location
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

     // API
     fun fetchAndStoreWeatherData(longitude: Double, latitude: Double): Flow<WeatherResponse?>

     // SharedPreferences
     fun setTemperatureUnit(unit: Temperature)
     fun getTemperatureUnit(): Temperature

     fun setWindSpeedUnit(unit: WindSpeed)
     fun getWindSpeedUnit(): WindSpeed

     fun setLocationStatus(location: Location)
     fun getLocationStatus(): Location

     fun setLanguage(language: Language)
     fun getLanguage(): Language

     fun setNotificationStatus(status: Boolean)
     fun getNotificationStatus(): Boolean

     fun saveLocation(latitude: Double, longitude: Double)
     fun getLocation(): Pair<Double, Double>?
}
