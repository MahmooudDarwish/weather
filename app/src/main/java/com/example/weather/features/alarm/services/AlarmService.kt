package com.example.weather.features.alarm.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import com.example.weather.R
import com.example.weather.databinding.AlarmLayoutBinding
import com.example.weather.utils.Utils

class AlarmService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var binding: AlarmLayoutBinding

    private val ALERT_CHANNEL_ID = "alert_channel_id"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        showAlarmOverlay(intent)
        createAlertChannel()
        val notification = getNotification()
        startForeground(1, notification)

        mediaPlayer = MediaPlayer.create(this, R.raw.rain_alarm).apply {
            isLooping = true
            start()
        }
        return START_STICKY

    }


    private fun getNotification(): Notification {
        return NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
                .setContentTitle("Weather Alert")
                .setContentText("Alarm is active")
                .setSmallIcon(R.drawable.ic_cloud)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

    }

    private fun showAlarmOverlay(intent: Intent?) {
        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = layoutInflater.inflate(R.layout.alarm_layout, null)


        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(overlayView, layoutParams)

        binding = DataBindingUtil.bind(overlayView)!!
        binding.alertTitle.text = intent?.getStringExtra("alarmTitle") ?: "Weather Alert!"
        binding.alertDesc.text = intent?.getStringExtra("alarmDescription") ?: "Check the weather!"
        binding.weatherAlertIcon.setImageResource(Utils().getWeatherIcon(intent?.getStringExtra("alarmIcon") ?: "01d"))

        val dismissButton: Button = overlayView.findViewById(R.id.dismissButton)
        dismissButton.setOnClickListener {
            stopSelf()

        }
    }

    private fun createAlertChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Alarm Channel",
              NotificationManager.IMPORTANCE_LOW

            ).apply {
                description = "Channel for alarm notifications"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

Log.d("AlarmService", "onDestroy called")
        if (::mediaPlayer.isInitialized) {
            Log.d("AlarmService", "MediaPlayer released")
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        if (::windowManager.isInitialized && ::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }

    }



    override fun onBind(intent: Intent?): IBinder? = null
}
