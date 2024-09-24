package com.example.weather.utils.local.room.local_data_source

import com.example.weather.utils.local.room.Dao.AlarmDao
import com.example.weather.utils.local.room.Dao.WeatherDao
import com.example.weather.utils.model.Local.AlarmEntity
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import kotlinx.coroutines.flow.Flow

class WeatherLocalDataSourceImpl(private val weatherDao: WeatherDao, private val alarmDao: AlarmDao) : WeatherLocalDataSource {

    override fun getCurrentWeather(lon: Double, lat: Double): Flow<WeatherEntity?> {
        return weatherDao.getFavoriteWeather(lon, lat)
    }

    override fun getDailyWeather(lon: Double, lat: Double): Flow<List<DailyWeatherEntity>> {
        return weatherDao.getDailyWeather(lon, lat)
    }

    override fun getHourlyWeather(lon: Double, lat: Double): Flow<List<HourlyWeatherEntity>> {
        return weatherDao.getHourlyWeather(lon, lat)
    }

    override fun getAllFavoriteWeather(): Flow<List<WeatherEntity>> {
        return weatherDao.getAllFavoriteWeather()
    }

    override suspend fun insertCurrentWeather(weather: WeatherEntity) {
        weatherDao.insertFavoriteWeather(weather)
    }

    override suspend fun insertDailyWeather(dailyWeather: List<DailyWeatherEntity>) {
        weatherDao.insertDailyWeather(dailyWeather)
    }

    override suspend fun insertHourlyWeather(hourlyWeather: List<HourlyWeatherEntity>) {
        weatherDao.insertHourlyWeather(hourlyWeather)
    }

    override suspend fun deleteCurrentWeather(lon: Double, lat: Double) {
        weatherDao.deleteCurrentWeather(lon, lat)
    }

    override suspend fun deleteDailyWeather(lon: Double, lat: Double) {
        weatherDao.deleteDailyWeather(lon, lat)
    }

    override suspend fun deleteHourlyWeather(lon: Double, lat: Double) {
        weatherDao.deleteHourlyWeather(lon, lat)
    }

    override suspend fun insertAlarm(alarm: AlarmEntity) {
       alarmDao.insertAlarm(alarm)
    }

    override suspend fun deleteAlarm(alarm: AlarmEntity) {
        alarmDao.deleteAlarm(alarm)
    }

    override fun getAllAlarm(): Flow<List<AlarmEntity>> {
       return  alarmDao.getAllAlarms()
    }
}
