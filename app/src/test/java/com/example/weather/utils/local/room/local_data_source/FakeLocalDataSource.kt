package com.example.weather.utils.local.room.local_data_source

import com.example.weather.utils.model.Local.AlarmEntity
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeLocalDataSource(
    private val mockedWeatherEntities: MutableList<WeatherEntity>,
    private val mockedHourlyWeatherEntities: MutableList<HourlyWeatherEntity>,
    private val mockedDailyWeatherEntities: MutableList<DailyWeatherEntity>
) : WeatherLocalDataSource {
    override fun getWeather(lon: Double, lat: Double): Flow<WeatherEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun insertDailyWeather(dailyWeather: List<DailyWeatherEntity>) {
        dailyWeather.forEach { weatherEntity ->
            val exists = mockedDailyWeatherEntities.any {
                it.longitude == weatherEntity.longitude &&
                        it.latitude == weatherEntity.latitude &&
                        it.dt == weatherEntity.dt
            }

            if (!exists) {
                mockedDailyWeatherEntities.add(weatherEntity)
            }
        }
    }
    override fun getDailyWeather(lon: Double, lat: Double): Flow<List<DailyWeatherEntity>> {
        return flow {
            val filteredWeatherList = mockedDailyWeatherEntities.filter {
                it.longitude == lon && it.latitude == lat
            }
            emit(filteredWeatherList)
        }
    }
    override suspend fun deleteDailyWeather(lon: Double, lat: Double) {
        mockedDailyWeatherEntities.removeAll {
            it.longitude == lon && it.latitude == lat
        }
    }

    override fun getHourlyWeather(lon: Double, lat: Double): Flow<List<HourlyWeatherEntity>> {
        TODO("Not yet implemented")
    }

    override fun getAllFavoriteWeather(): Flow<List<WeatherEntity>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertCurrentWeather(weather: WeatherEntity) {
        TODO("Not yet implemented")
    }



    override suspend fun insertHourlyWeather(hourlyWeather: List<HourlyWeatherEntity>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteCurrentWeather(lon: Double, lat: Double) {
        TODO("Not yet implemented")
    }



    override suspend fun deleteHourlyWeather(lon: Double, lat: Double) {
        TODO("Not yet implemented")
    }

    override suspend fun insertAlarm(alarm: AlarmEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAlarmById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun getAllAlarm(): Flow<List<AlarmEntity>> {
        TODO("Not yet implemented")
    }


}