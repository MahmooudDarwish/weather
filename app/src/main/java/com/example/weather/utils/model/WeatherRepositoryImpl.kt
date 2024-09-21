package com.example.weather.utils.model


import com.example.weather.utils.remote.WeatherRemoteDataSource


import com.example.weather.utils.local.shared_perefernces.SharedPreferences
import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.LocationStatus
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

        val lang = when(getLanguage()){
            Language.ENGLISH -> "en"
            Language.ARABIC -> "ar"
        }
        return remoteDataSource.getCurrentWeather(
            latitude = latitude.toString(),
            longitude = longitude.toString(),
            lang = lang
        )
        //TODO: Add additional logic here to store data in localDataSource or handle preferences
    }

    override fun fetchHourlyWeatherData(
        longitude: Double,
        latitude: Double
    ): Flow<HourlyWeatherResponse?> {
        val lang = when(getLanguage()){
            Language.ENGLISH -> "en"
            Language.ARABIC -> "ar"
        }

        return remoteDataSource.getHourlyWeather(
            latitude = latitude.toString(),
            longitude = longitude.toString(),
            lang = lang
        )    }


    override fun get5DayForecast(
        longitude: Double,
        latitude: Double
    ): Flow<ForecastResponse?> {

        val lang = when(getLanguage()){
            Language.ENGLISH -> "en"
            Language.ARABIC -> "ar"
        }

        return remoteDataSource.get5DayForecast(
            latitude = latitude.toString(),
            longitude = longitude.toString(),
            lang = lang
        )
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

    override fun setLocationStatus(locationStatus: LocationStatus) {
        sharedPreferences.setLocationStatus(locationStatus)
    }

    override fun getLocationStatus(): LocationStatus {
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

    override fun saveCurrentLocation(latitude: Double, longitude: Double) {
        sharedPreferences.setLocation(latitude = latitude, longitude = longitude)
    }

    override fun getCurrentLocation(): Pair<Double, Double>? {
        return sharedPreferences.getLocation()
    }

    override fun setFirstLaunchCompleted() {
        sharedPreferences.setFirstLaunchCompleted(true)
    }

    override fun isFirstLaunch(): Boolean {
        return sharedPreferences.isFirstLaunch()
    }


}
