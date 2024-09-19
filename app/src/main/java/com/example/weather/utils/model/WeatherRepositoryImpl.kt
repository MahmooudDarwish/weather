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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException

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
                instance ?: WeatherRepositoryImpl(
                    remoteDataSource,
                    localDataSource,
                    sharedPreferences
                )
                    .also { instance = it }
            }
        }
    }


    ///API
    override fun fetchAndStoreWeatherData(longitude: Double, latitude: Double): Flow<WeatherResponse?> {
        //TODO: Get language and metric from preferences
        return remoteDataSource.getCurrentWeather(
            latitude = latitude.toString(),
            longitude = longitude.toString(),
            metric = "metric",
            lang = "en"
        )
        //TODO: Add additional logic here to store data in localDataSource or handle preferences
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

    fun setLocationStatus(location: Location) {
        sharedPreferences.setLocationStatus(location)
    }

    fun getLocationStatus(): Location {
        return sharedPreferences.setLocationStatus()
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

    fun saveLocation(latitude: Double, longitude: Double) {
        sharedPreferences.setLocation(latitude = latitude, longitude = longitude)
    }

    fun getLocation(): Pair<Double, Double>? {
        return sharedPreferences.getLocation()
    }

}
