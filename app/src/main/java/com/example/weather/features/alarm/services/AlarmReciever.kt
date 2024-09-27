package com.example.weather.features.alarm.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.weather.utils.constants.Keys


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

}