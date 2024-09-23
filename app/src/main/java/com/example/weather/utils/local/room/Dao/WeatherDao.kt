package com.example.weather.utils.local.room.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    // Insert Current Weather
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrentWeather(weather: WeatherEntity)

    @Query("SELECT * FROM current_weather WHERE longitude = :lon AND latitude = :lat")
    fun getCurrentWeather(lon: Double, lat: Double): Flow<WeatherEntity?>

    // Insert Daily Weather
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyWeather(dailyWeather: List<DailyWeatherEntity>)

    @Query("SELECT * FROM daily_weather WHERE longitude = :lon AND latitude = :lat")
    fun getDailyWeather(lon: Double, lat: Double): Flow<List<DailyWeatherEntity>>

    // Insert Hourly Weather
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyWeather(hourlyWeather: List<HourlyWeatherEntity>)

    @Query("SELECT * FROM hourly_weather WHERE longitude = :lon AND latitude = :lat")
    fun getHourlyWeather(lon: Double, lat: Double): Flow<List<HourlyWeatherEntity>>
}
