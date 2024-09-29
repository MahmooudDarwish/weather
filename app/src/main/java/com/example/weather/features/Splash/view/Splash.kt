package com.example.weather.features.Splash.view

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weather.databinding.ActivitySplashBinding
import com.example.weather.features.landing.view.LandingActivity
import com.example.weather.features.settings.view_model.SettingsViewModel
import com.example.weather.features.settings.view_model.SettingsViewModelFactory
import com.example.weather.utils.managers.SharedDataManager
import com.example.weather.utils.constants.Keys
import com.example.weather.utils.enums.Language
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferencesManager
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class Splash : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)


        lifecycleScope.launch {
            val language = SharedDataManager.languageFlow.first()
            when (language) {
                Language.ENGLISH -> updateLocale("en")
                Language.ARABIC -> updateLocale("ar")
            }
        }
        recreate()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setUpLottieAnimation()

        val settingsFactory = SettingsViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
                localDataSource = WeatherLocalDataSourceImpl(
                    AppDatabase.getDatabase(this).weatherDao(),
                    AppDatabase.getDatabase(this).alarmDao()
                ),
                sharedPreferences = SharedPreferencesManager(this.getSharedPreferences(
                    Keys.SHARED_PREFERENCES_NAME, MODE_PRIVATE
                ))
            )
        )
        val settingsViewModel = ViewModelProvider(this, settingsFactory).get(SettingsViewModel::class.java)

        lifecycleScope.launch {
            settingsViewModel.languageFlow.collect { language ->
                Log.i("DEBGUGG", "language: $language")
                when (language) {
                    Language.ENGLISH -> updateLocale("en")
                    Language.ARABIC -> updateLocale("ar")
                }
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LandingActivity::class.java))
            finish()
        }, 3000)
    }
    private fun setUpLottieAnimation() {
        binding.lottieAnimationView.playAnimation()
        binding.lottieAnimationView.speed = 1.5f
    }
    private fun updateLocale(language:String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources: Resources = this.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        applicationContext.createConfigurationContext(config)
        applicationContext.resources.updateConfiguration(config, applicationContext.resources.displayMetrics)
    }
}
