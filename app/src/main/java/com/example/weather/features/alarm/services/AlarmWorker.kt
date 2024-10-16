package com.example.weather.features.alarm.services

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import com.example.weather.utils.constants.Keys
import androidx.work.WorkerParameters
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferencesManager
import com.example.weather.utils.model.repository.WeatherRepository
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl

class AlarmWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val repo: WeatherRepository by lazy {
        WeatherRepositoryImpl.getInstance(
            remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
            localDataSource = WeatherLocalDataSourceImpl(
                AppDatabase.getDatabase(context).weatherDao(),
                AppDatabase.getDatabase(context).alarmDao()
            ),
            sharedPreferences = SharedPreferencesManager(context.getSharedPreferences(Keys.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE))
        )
    }
    override suspend fun doWork(): Result {

        val title = inputData.getString(Keys.ALARM_TITLE_KEY) ?: "Weather Alert!"
        val description = inputData.getString(Keys.ALARM_DESCRIPTION_KEY) ?: "Check the weather!"
        val weatherIcon = inputData.getString(Keys.ALARM_ICON_KEY) ?: "10d"
        val id = inputData.getLong(Keys.ALARM_ID_KEY, 0)
        val dismissTime = inputData.getLong(Keys.ALARM_DISMISS_KEY, 0) // Get dismiss time

        val notificationStatus = repo.getNotificationStatus()



        if (!notificationStatus) {
            Log.d("NotificationWorker", "Notification is disabled, skipping notification.")
            repo.deleteAlarm(id)

            return Result.success()
        }

        val alarmIntent = Intent(applicationContext, AlarmService::class.java).apply {
            putExtra(Keys.ALARM_TITLE_KEY, title)
            putExtra(Keys.ALARM_DESCRIPTION_KEY, description)
            putExtra(Keys.ALARM_ICON_KEY, weatherIcon)
            putExtra(Keys.ALARM_ID_KEY, id)
            putExtra(Keys.ALARM_DISMISS_KEY, dismissTime)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(alarmIntent)
        } else {
            applicationContext.startService(alarmIntent)
        }

        return Result.success()
    }
}