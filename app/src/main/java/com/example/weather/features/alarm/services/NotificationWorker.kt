package com.example.weather.features.alarm.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.weather.R

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("NotificationWorker", "doWork() called")
        val context = applicationContext
        val title: String = inputData.getString("alarmTitle") ?: "Weather Alert!"
        val body: String = inputData.getString("alarmDescription") ?: "Check the weather!"

        createNotificationChannel()

        val notificationManager = NotificationManagerCompat.from(context)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_cloud)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)

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
