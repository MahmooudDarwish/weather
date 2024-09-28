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
        val notificationStatus = repo.getNotificationStatus()


        if (!notificationStatus) {
            Log.d("NotificationWorker", "Notification is disabled, skipping notification.")
            repo.deleteAlarm(id)

            return Result.success()
        }

        // Start your service to handle the alarm
        val alarmIntent = Intent(applicationContext, AlarmService::class.java).apply {
            putExtra(Keys.ALARM_TITLE_KEY, title)
            putExtra(Keys.ALARM_DESCRIPTION_KEY, description)
            putExtra(Keys.ALARM_ICON_KEY, weatherIcon)
            putExtra(Keys.ALARM_ID_KEY, id)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(alarmIntent)
        } else {
            applicationContext.startService(alarmIntent)
        }

        return Result.success()
    }
}


/*
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val alarmIntent = Intent(context, AlarmService::class.java)
        val title : String = intent?.getStringExtra(Keys.ALARM_TITLE_KEY) ?: "Weather Alert!"
        val description : String = intent?.getStringExtra(Keys.ALARM_DESCRIPTION_KEY) ?: "Check the weather!"
        val weatherIcon: String = intent?.getStringExtra(Keys.ALARM_ICON_KEY) ?: "10d"
        val id:Long = intent?.getLongExtra(Keys.ALARM_ID_KEY, 0) ?: 0


        alarmIntent.putExtra(Keys.ALARM_TITLE_KEY, title)
        alarmIntent.putExtra(Keys.ALARM_DESCRIPTION_KEY, description)
        alarmIntent.putExtra(Keys.ALARM_ICON_KEY, weatherIcon)
        alarmIntent.putExtra(Keys.ALARM_ID_KEY, id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(alarmIntent)
        } else {
            context.startService(alarmIntent)
        }
    }

}*/