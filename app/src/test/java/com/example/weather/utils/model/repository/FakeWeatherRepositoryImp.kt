package com.example.weather.utils.model.repository

import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import com.example.weather.utils.model.API.DailyWeatherResponse
import com.example.weather.utils.model.API.HourlyWeatherResponse
import com.example.weather.utils.model.API.WeatherResponse
import com.example.weather.utils.model.Local.AlarmEntity
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class FakeWeatherRepositoryImp(
) : WeatherRepository{
    private val alarms = mutableListOf<AlarmEntity>()



    override suspend fun insertAlarm(alarm: AlarmEntity) {
        if (alarms.any { it.startDate == alarm.startDate }) {
            throw IllegalArgumentException("An alarm with the same ID already exists.")
        }
        alarms.add(alarm)
    }

    override suspend fun getAllAlarms(): Flow<List<AlarmEntity>> =  flow {
        emit(alarms)
    }

    override suspend fun deleteAlarm(id: Long) {
        val alarmToDelete = alarms.find { it.startDate == id }
        println(alarmToDelete)

        if (alarmToDelete != null) {
            alarms.remove(alarmToDelete)
            println(alarms)
        } else {
            throw NoSuchElementException("Alarm with id $id not found.")
        }
    }


    override fun fetchWeatherData(longitude: Double, latitude: Double): Flow<WeatherResponse> {
        TODO("Not yet implemented")
    }

    override fun fetchHourlyWeatherData(longitude: Double, latitude: Double): Flow<HourlyWeatherResponse> {
        TODO("Not yet implemented")

    }

    override fun get5DayForecast(longitude: Double, latitude: Double): Flow<DailyWeatherResponse?> {
        TODO("Not yet implemented")

    }

    override suspend fun insertWeather(weather: WeatherEntity) {
        TODO("Not yet implemented")

    }

    override suspend fun insertDailyWeather(dailyWeather: List<DailyWeatherEntity>) {
        TODO("Not yet implemented")

    }

    override suspend fun insertHourlyWeather(hourlyWeather: List<HourlyWeatherEntity>) {
        TODO("Not yet implemented")

    }

    override suspend fun getWeather(lon: Double, lat: Double): Flow<WeatherEntity> {
        TODO("Not yet implemented")

    }

    override suspend fun getDailyWeather(lon: Double, lat: Double): Flow<List<DailyWeatherEntity>> {
        TODO("Not yet implemented")

    }

    override suspend  fun getHourlyWeather(lon: Double, lat: Double): Flow<List<HourlyWeatherEntity>> {
        TODO("Not yet implemented")

    }

    override suspend fun deleteFavoriteWeather(lon: Double, lat: Double) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteFavoriteDailyWeather(lon: Double, lat: Double) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteFavoriteHourlyWeather(lon: Double, lat: Double) {
        TODO("Not yet implemented")
    }

    override fun get30DayForecast(longitude: Double, latitude: Double): Flow<DailyWeatherResponse?> {
        TODO("Not yet implemented")
    }



    override suspend fun getAllFavoriteWeather(): Flow<List<WeatherEntity>> {
        TODO("Not yet implemented")
    }

    override fun setTemperatureUnit(unit: Temperature) {
        TODO("Not yet implemented")
    }

    override fun getTemperatureUnit(): Temperature {
        TODO("Not yet implemented")
    }

    override fun setWindSpeedUnit(unit: WindSpeed) {
        TODO("Not yet implemented")
    }

    override fun getWindSpeedUnit(): WindSpeed {
        TODO("Not yet implemented")
    }

    override fun setLocationStatus(locationStatus: LocationStatus) {
        TODO("Not yet implemented")
    }

    override fun getLocationStatus(): LocationStatus {
        TODO("Not yet implemented")
    }

    override fun setLanguage(language: Language) {
        TODO("Not yet implemented")
    }

    override fun getLanguage(): Language {
        TODO("Not yet implemented")
    }

    override fun setNotificationStatus(status: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getNotificationStatus(): Boolean {
        TODO("Not yet implemented")
    }

    override fun saveCurrentLocation(latitude: Double, longitude: Double) {
        TODO("Not yet implemented")
    }

    override fun getCurrentLocation(): Pair<Double, Double>? {
        TODO("Not yet implemented")
    }

    override fun setFirstLaunchCompleted() {
        TODO("Not yet implemented")
    }

    override fun isFirstLaunch(): Boolean {
        TODO("Not yet implemented")
    }

}