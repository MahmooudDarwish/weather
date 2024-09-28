package com.example.weather.utils.local.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.weather.utils.local.room.Dao.AlarmDao
import com.example.weather.utils.local.room.Dao.WeatherDao
import com.example.weather.utils.model.Local.AlarmEntity
import com.example.weather.utils.model.Local.DailyWeatherEntity
import com.example.weather.utils.model.Local.HourlyWeatherEntity
import com.example.weather.utils.model.Local.WeatherEntity
import java.util.concurrent.Executors


@Database(
    entities = [
        WeatherEntity::class,
        DailyWeatherEntity::class,
        HourlyWeatherEntity::class,
        AlarmEntity::class

    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
    abstract fun alarmDao(): AlarmDao



    companion object {
        val queryCallback = object : RoomDatabase.QueryCallback {
            override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
                Log.d("RoomQuery", "Executed query: $sqlQuery, with args: $bindArgs")
            }
        }
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "weather_database"
                ).setQueryCallback(queryCallback, Executors.newSingleThreadExecutor()) // Use an executor for callback
                    .fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}