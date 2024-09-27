package com.example.weather.features.alarm.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import com.example.weather.utils.constants.Keys
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferencesManager
import com.example.weather.utils.model.repository.WeatherRepository
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmService : Service() {

    private lateinit var repo: WeatherRepository

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var binding: AlarmLayoutBinding

    private val ALERT_CHANNEL_ID = "alert_channel_id"

    override fun onCreate() {
        super.onCreate()
        repo =  WeatherRepositoryImpl.getInstance(
            remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
            localDataSource = WeatherLocalDataSourceImpl(
                AppDatabase.getDatabase(this).weatherDao(),
                AppDatabase.getDatabase(this).alarmDao()
            ),
            sharedPreferences = SharedPreferencesManager(this.getSharedPreferences(Keys.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE))

        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val id:Long = intent?.getLongExtra(Keys.ALARM_ID_KEY, 0) ?: 0

        createAlertChannel()

        if (!repo.getNotificationStatus()) {
            startForeground(1,  NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build())

            CoroutineScope(Dispatchers.IO).launch {
                repo.deleteAlarm(id)
            }

            stopSelf()
            return START_NOT_STICKY
        }

        showAlarmOverlay(intent)
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
        binding.alertTitle.text = intent?.getStringExtra(Keys.ALARM_TITLE_KEY) ?: "Weather Alert!"
        binding.alertDesc.text = intent?.getStringExtra(Keys.ALARM_DESCRIPTION_KEY) ?: "Check the weather!"
        binding.weatherAlertIcon.setImageResource(Utils().getWeatherIcon(intent?.getStringExtra(Keys.ALARM_ICON_KEY) ?: "01d"))

        val id :Long = intent?.getLongExtra(Keys.ALARM_ID_KEY, 0) ?: 0

        val dismissButton: Button = overlayView.findViewById(R.id.dismissButton)
        dismissButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                repo.deleteAlarm(id)
            }
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
