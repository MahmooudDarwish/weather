package com.example.weather.features.alarm.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val alarmIntent = Intent(context, AlarmService::class.java)
        val title : String = intent?.getStringExtra("alarmTitle") ?: "Weather Alert!"
        val description : String = intent?.getStringExtra("alarmDescription") ?: "Check the weather!"
        val weatherIcon: String = intent?.getStringExtra("weatherIcon") ?: "10d"

        alarmIntent.putExtra("alarmTitle", title)
        alarmIntent.putExtra("alarmDescription", description)
        alarmIntent.putExtra("weatherIcon", weatherIcon)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(alarmIntent)
        } else {
            context.startService(alarmIntent)
        }
    }

}