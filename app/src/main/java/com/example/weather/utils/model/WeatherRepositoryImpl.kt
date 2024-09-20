package com.example.weather.utils.model


import com.example.weather.utils.remote.WeatherRemoteDataSource


import com.example.weather.utils.local.shared_perefernces.SharedPreferences
import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.Location
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSource
import kotlinx.coroutines.flow.Flow


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
    override fun setTemperatureUnit(unit: Temperature) {
        sharedPreferences.setTemperatureUnit(unit)
    }

    override fun getTemperatureUnit(): Temperature {
        return sharedPreferences.getTemperatureUnit()
    }

    override fun setWindSpeedUnit(unit: WindSpeed) {
        sharedPreferences.setWindSpeedUnit(unit)
    }

    override fun getWindSpeedUnit(): WindSpeed {
        return sharedPreferences.getWindSpeedUnit()
    }

    override fun setLocationStatus(location: Location) {
        sharedPreferences.setLocationStatus(location)
    }

    override fun getLocationStatus(): Location {
        return sharedPreferences.getLocationStatus()
    }

    override fun setLanguage(language: Language) {
        sharedPreferences.setLanguage(language)
    }

    override fun getLanguage(): Language {
        return sharedPreferences.getLanguage()
    }

    override fun setNotificationStatus(status: Boolean) {
        sharedPreferences.setNotificationStatus(status)
    }

    override fun getNotificationStatus(): Boolean {
        return sharedPreferences.getNotificationStatus()
    }

    override fun saveLocation(latitude: Double, longitude: Double) {
        sharedPreferences.setLocation(latitude = latitude, longitude = longitude)
    }

    override fun getLocation(): Pair<Double, Double>? {
        return sharedPreferences.getLocation()
    }

}
