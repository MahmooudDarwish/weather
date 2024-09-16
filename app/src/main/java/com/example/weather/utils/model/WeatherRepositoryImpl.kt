package com.example.weather.utils.model

import com.example.weather.utils.local.room.Dao.ForecastDao
import com.example.weather.utils.local.room.Dao.WeatherDao
import com.example.weather.utils.remote.WeatherRemoteDataSource


import com.example.weather.utils.local.shared_perefernces.SharedPreferences
import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.Location
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepositoryImpl private constructor(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val weatherDao: WeatherDao,
    private val forecastDao: ForecastDao,
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        @Volatile
        private var instance: WeatherRepositoryImpl? = null

        fun getInstance(
            remoteDataSource: WeatherRemoteDataSource,
            weatherDao: WeatherDao,
            forecastDao: ForecastDao,
            sharedPreferences: SharedPreferences
        ): WeatherRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: WeatherRepositoryImpl(remoteDataSource, weatherDao, forecastDao, sharedPreferences)
                    .also { instance = it }
            }
        }
    }

    ///API
    suspend fun fetchAndStoreWeatherData(location: String) {
        withContext(Dispatchers.IO) {
            val response = remoteDataSource
        }
    }

    ///ROOM DATABASE
  //  suspend fun insertWeatherData(weatherEntity: WeatherEntity) {
   //     weatherDao.insertWeatherData(weatherData)
    //}






    ///SharedPreferences
    fun setTemperatureUnit(unit: Temperature) {
        sharedPreferences.setTemperatureUnit(unit)
    }

    fun getTemperatureUnit(): Temperature {
        return sharedPreferences.getTemperatureUnit()
    }

    fun setWindSpeedUnit(unit: WindSpeed) {
        sharedPreferences.setWindSpeedUnit(unit)
    }

    fun getWindSpeedUnit(): WindSpeed {
        return sharedPreferences.getWindSpeedUnit()
    }

    fun setLocation(location: Location) {
        sharedPreferences.setLocation(location)
    }

    fun getLocation(): Location {
        return sharedPreferences.getLocation()
    }

    fun setLanguage(language: Language) {
        sharedPreferences.setLanguage(language)
    }

    fun getLanguage(): Language {
        return sharedPreferences.getLanguage()
    }

    fun setNotificationStatus(status: Boolean) {
        sharedPreferences.setNotificationStatus(status)
    }

    fun getNotificationStatus(): Boolean {
        return sharedPreferences.getNotificationStatus()
    }
}
