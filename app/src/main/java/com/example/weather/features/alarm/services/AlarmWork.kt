package com.example.weather.features.alarm.services

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
class AlarmWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val alarmIcon = inputData.getString("alarmIcon") ?: ""
        val alarmTitle = inputData.getString("alarmTitle") ?: "Default Title"
        val alarmDescription = inputData.getString("alarmDescription") ?: "Default Description"

        val intent = Intent(applicationContext, AlarmService::class.java).apply {
            putExtra("alarmIcon", alarmIcon)
            putExtra("alarmTitle", alarmTitle)
            putExtra("alarmDescription", alarmDescription)
        }

        ContextCompat.startForegroundService(applicationContext, intent)

        return Result.success()
    }
}
