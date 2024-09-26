package com.example.weather.features.alarm.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.WorkerParameters

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import com.example.weather.R
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferences
import com.example.weather.utils.model.repository.WeatherRepository
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl


class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val repo: WeatherRepository by lazy {
        WeatherRepositoryImpl.getInstance(
            remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
            localDataSource = WeatherLocalDataSourceImpl(
                AppDatabase.getDatabase(context).weatherDao(),
                AppDatabase.getDatabase(context).alarmDao()
            ),
            sharedPreferences = SharedPreferences(context)
        )
    }

    override suspend fun doWork(): Result {
        val context = applicationContext

        Log.d("NotificationWorker", "doWork() called")
        val title: String = inputData.getString("alarmTitle") ?: "Weather Alert!"
        val body: String = inputData.getString("alarmDescription") ?: "Check the weather!"
        val id: Long = inputData.getLong("alarmId", 0)
        val notificationStatus = repo.getNotificationStatus()


        if (!notificationStatus) {
            Log.d("NotificationWorker", "Notification is disabled, skipping notification.")

            repo.deleteAlarm(id)

            return Result.success()
        }

        createNotificationChannel()

        val notificationManager = NotificationManagerCompat.from(context)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_cloud)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(1, notification)
        }

        repo.deleteAlarm(id)
        return Result.success()
    }

    private fun createNotificationChannel() {
        Log.d("NotificationWorker", "createNotificationChannel() called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Notification Channel",
                NotificationManager.IMPORTANCE_HIGH

            ).apply {
                description = "Channel for notifications"
            }
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "notification_channel_id"
    }
}
