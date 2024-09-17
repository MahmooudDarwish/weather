package com.example.weather.utils.model

import com.example.weather.utils.local.room.Dao.ForecastDao
import com.example.weather.utils.local.room.Dao.WeatherDao
import com.example.weather.utils.remote.WeatherRemoteDataSource


import com.example.weather.utils.local.shared_perefernces.SharedPreferences
import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.Location
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepositoryImpl private constructor(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val localDataSource: WeatherLocalDataSource,
    private val sharedPreferences: SharedPreferences
) : WeatherRepository {

    companion object {
        @Volatile
        private var instance: WeatherRepositoryImpl? = null

        fun getInstance(
            remoteDataSource: WeatherRemoteDataSource,
            localDataSource: WeatherLocalDataSource,
            sharedPreferences: SharedPreferences
        ): WeatherRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: WeatherRepositoryImpl(remoteDataSource, localDataSource, sharedPreferences)
                    .also { instance = it }
            }
        }
    }

    ///API
    suspend fun fetchAndStoreWeatherData(longtitude: Double, latitude: Double) {
        ///TODO: get the current language and metric from shared preferences

        withContext(Dispatchers.IO) {
            val response = remoteDataSource.getCurrentWeather(
                longitude = longtitude.toString(),
                latitude = latitude.toString(),
                lang = "en",
                metric = "metric"
            )
        }
    }

    ///ROOM DATABASE






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
