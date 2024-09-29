package com.example.weather.utils.model.repository


import android.util.Log
import com.example.weather.utils.remote.WeatherRemoteDataSource


import com.example.weather.utils.local.shared_perefernces.SharedPreferencesManager
import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSource
import com.example.weather.utils.local.shared_perefernces.ISharedPreferencesManager
import com.example.weather.utils.model.API.WeatherResponse
import com.example.weather.utils.model.API.DailyWeatherResponse
import com.example.weather.utils.model.API.HourlyWeatherResponse
import com.example.weather.utils.model.Local.AlarmEntity
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import kotlinx.coroutines.flow.Flow


class WeatherRepositoryImpl private constructor(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val localDataSource: WeatherLocalDataSource,
    private val sharedPreferences: ISharedPreferencesManager
) : WeatherRepository {

    companion object {
        @Volatile
        private var instance: WeatherRepositoryImpl? = null

        fun getInstance(
            remoteDataSource: WeatherRemoteDataSource,
            localDataSource: WeatherLocalDataSource,
            sharedPreferences: ISharedPreferencesManager
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
    override fun fetchWeatherData(longitude: Double, latitude: Double): Flow<WeatherResponse?> {

        val lang = when(getLanguage()){
            Language.ENGLISH -> "en"
            Language.ARABIC -> "ar"
        }
        return remoteDataSource.getCurrentWeather(
            latitude = latitude.toString(),
            longitude = longitude.toString(),
            lang = lang
        )
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
        )

    }


    override fun get5DayForecast(
        longitude: Double,
        latitude: Double
    ): Flow<DailyWeatherResponse?> {

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

    override fun get30DayForecast(
        longitude: Double,
        latitude: Double
    ): Flow<DailyWeatherResponse?> {
        val lang = when(getLanguage()){
            Language.ENGLISH -> "en"
            Language.ARABIC -> "ar"
        }

        return remoteDataSource.get30DayForecast(
            latitude = latitude.toString(),
            longitude = longitude.toString(),
            lang = lang
        )
    }

    ///ROOM DATABASE

    override suspend fun getWeather(lon: Double, lat: Double): Flow<WeatherEntity> {
        return localDataSource.getWeather(lon = lon,lat = lat)
    }

    override suspend fun getDailyWeather(lon: Double, lat: Double): Flow<List<DailyWeatherEntity>> {
        return localDataSource.getDailyWeather(lon = lon,lat = lat)
    }

    override suspend fun getHourlyWeather(lon: Double, lat: Double): Flow<List<HourlyWeatherEntity>> {
        return localDataSource.getHourlyWeather(lon = lon,lat = lat)
    }

    override suspend fun getAllFavoriteWeather(): Flow<List<WeatherEntity>> {
        return localDataSource.getAllFavoriteWeather()
    }

    override suspend fun insertWeather(weather: WeatherEntity) {
        localDataSource.insertCurrentWeather(weather)
    }

    override suspend fun insertDailyWeather(dailyWeather: List<DailyWeatherEntity>) {
        localDataSource.insertDailyWeather(dailyWeather)
    }

    override suspend fun insertHourlyWeather(hourlyWeather: List<HourlyWeatherEntity>) {
        localDataSource.insertHourlyWeather(hourlyWeather)
    }

    override suspend fun deleteFavoriteWeather(lon: Double, lat: Double) {
        localDataSource.deleteCurrentWeather(lon, lat)
    }

    override suspend fun deleteFavoriteDailyWeather(lon: Double, lat: Double) {
        localDataSource.deleteDailyWeather(lon, lat)
    }

    override suspend fun deleteFavoriteHourlyWeather(lon: Double, lat: Double) {
        localDataSource.deleteHourlyWeather(lon, lat)
    }

    override suspend fun insertAlarm(alarm: AlarmEntity) {
        localDataSource.insertAlarm(alarm)
    }

    override suspend fun getAllAlarms(): Flow<List<AlarmEntity>> {
       return localDataSource.getAllAlarm()
    }

    override suspend fun deleteAlarm(id: Long) {
        localDataSource.deleteAlarmById(id)
    }


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
