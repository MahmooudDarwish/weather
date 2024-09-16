package com.example.weather.utils.local.room.Dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weather.utils.model.ForecastEntity

@Dao
interface ForecastDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecast(forecast: List<ForecastEntity>)

    @Query("SELECT * FROM forecast WHERE city = :city ORDER BY dateTime ASC")
   suspend fun getForecast(city: String): List<ForecastEntity>
}

