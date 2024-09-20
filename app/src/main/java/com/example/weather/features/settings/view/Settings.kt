package com.example.weather.features.settings.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.lifecycle.ViewModelProvider
import com.example.weather.R
import com.example.weather.features.settings.view_model.SettingsViewModel
import com.example.weather.features.settings.view_model.SettingsViewModelFactory
import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferences
import com.example.weather.utils.model.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl

class Settings : Fragment() {

    private lateinit var viewModel: SettingsViewModel
    private lateinit var factory: SettingsViewModelFactory
    private lateinit var radioGroupTemperature: RadioGroup
    private lateinit var radioGroupWindSpeed: RadioGroup
    private lateinit var radioGroupNotification: RadioGroup
    private lateinit var radioGroupLanguage: RadioGroup
    private lateinit var radioGroupLocation: RadioGroup


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Create ViewModel using the factory

        factory = SettingsViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
                localDataSource = WeatherLocalDataSourceImpl(
                    AppDatabase.getDatabase(requireActivity()).weatherDao(),
                    AppDatabase.getDatabase(requireActivity()).forecastDao()
                ),
                sharedPreferences = SharedPreferences(requireActivity())

            )
        )
        viewModel = ViewModelProvider(this, factory).get(SettingsViewModel::class.java)


        initUI(view)
        getSavedSettings()
        setUpListeners()

        return view
    }

    private fun setUpListeners() {
        radioGroupTemperature.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbKelvin -> viewModel.saveTemperatureUnit(Temperature.KELVIN)
                R.id.rbCelsius -> viewModel.saveTemperatureUnit(Temperature.CELSIUS)
                R.id.rbFahrenheit -> viewModel.saveTemperatureUnit(Temperature.FAHRENHEIT)
            }
        }

        radioGroupWindSpeed.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbMeterPerSecond -> viewModel.saveWindSpeedUnit(WindSpeed.METERS_PER_SECOND)
                R.id.rbMilePerHour -> viewModel.saveWindSpeedUnit(WindSpeed.MILES_PER_HOUR)
            }
        }

        radioGroupNotification.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbDisable -> viewModel.saveNotificationStatus(false)
                R.id.rbEnable -> viewModel.saveNotificationStatus(true)
            }
        }

        radioGroupLanguage.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbEnglish -> viewModel.saveLanguage(Language.ENGLISH)
                R.id.rbArabic -> viewModel.saveLanguage(Language.ARABIC)
            }
        }

        radioGroupLocation.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbSettingsGPS -> viewModel.saveLocationStatus(LocationStatus.GPS)
                R.id.rbSettingsMap -> viewModel.saveLocationStatus(LocationStatus.MAP)
            }
        }

    }

    private fun initUI(view: View) {
        radioGroupTemperature = view.findViewById(R.id.rgTemperature)
        radioGroupWindSpeed = view.findViewById(R.id.rgWindSpeed)
        radioGroupNotification = view.findViewById(R.id.rgNotifications)
        radioGroupLanguage = view.findViewById(R.id.rgLanguage)
        radioGroupLocation = view.findViewById(R.id.rgLocation)

    }

    private fun getSavedSettings() {
        when (viewModel.getTemperatureUnit()) {
            Temperature.CELSIUS -> radioGroupTemperature.check(R.id.rbCelsius)
            Temperature.FAHRENHEIT -> radioGroupTemperature.check(R.id.rbFahrenheit)
            Temperature.KELVIN -> radioGroupTemperature.check(R.id.rbKelvin)
        }

        when (viewModel.getWindSpeedUnit()) {
            WindSpeed.METERS_PER_SECOND -> radioGroupWindSpeed.check(R.id.rbMeterPerSecond)
            WindSpeed.MILES_PER_HOUR -> radioGroupWindSpeed.check(R.id.rbMilePerHour)
        }
        when (viewModel.getNotificationStatus()) {
            false -> radioGroupNotification.check(R.id.rbDisable)
            true -> radioGroupNotification.check(R.id.rbEnable)
        }
        when (viewModel.getLanguage()) {
            Language.ENGLISH -> radioGroupLanguage.check(R.id.rbEnglish)
            Language.ARABIC -> radioGroupLanguage.check(R.id.rbArabic)
        }

        Log.d("TAG", "getSavedSettings: ${viewModel.getLocationStatus()}")
        when (viewModel.getLocationStatus()) {
            LocationStatus.GPS -> radioGroupLocation.check(R.id.rbSettingsGPS)
            LocationStatus.MAP -> radioGroupLocation.check(R.id.rbSettingsMap)
        }

    }
}
