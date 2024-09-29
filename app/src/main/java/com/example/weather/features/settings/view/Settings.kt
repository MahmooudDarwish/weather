package com.example.weather.features.settings.view


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weather.R
import com.example.weather.databinding.FragmentSettingsBinding
import com.example.weather.features.settings.view_model.SettingsViewModel
import com.example.weather.features.settings.view_model.SettingsViewModelFactory
import com.example.weather.utils.constants.Keys
import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferencesManager
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.util.Locale

class Settings : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    private lateinit var viewModel: SettingsViewModel
    private lateinit var factory: SettingsViewModelFactory
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isListenerEnabled = true



    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            checkGpsStatusAndFetchLocation()
        } else {
            showGpsPermissionDeniedDialog()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())



        factory = SettingsViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
                localDataSource = WeatherLocalDataSourceImpl(
                    AppDatabase.getDatabase(requireActivity()).weatherDao(),
                    AppDatabase.getDatabase(requireActivity()).alarmDao()
                ),
                sharedPreferences = SharedPreferencesManager(requireActivity().getSharedPreferences(
                    Keys.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE))
            )
        )
        viewModel = ViewModelProvider(this, factory).get(SettingsViewModel::class.java)

        collectSettingsFlows()
        setUpListeners()

        return binding.root
    }

    private fun setUpListeners() {
        binding.rgTemperature.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbKelvin -> viewModel.saveTemperatureUnit(Temperature.KELVIN)
                R.id.rbCelsius -> viewModel.saveTemperatureUnit(Temperature.CELSIUS)
                R.id.rbFahrenheit -> viewModel.saveTemperatureUnit(Temperature.FAHRENHEIT)
            }
        }

        binding.rgWindSpeed.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbMeterPerSecond -> viewModel.saveWindSpeedUnit(WindSpeed.METERS_PER_SECOND)
                R.id.rbMilePerHour -> viewModel.saveWindSpeedUnit(WindSpeed.MILES_PER_HOUR)
            }
        }

        binding.rgNotifications.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbDisable -> viewModel.saveNotificationStatus(false)
                R.id.rbEnable -> viewModel.saveNotificationStatus(true)
            }
        }

        binding.rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbEnglish -> {
                    viewModel.saveLanguage(Language.ENGLISH)
                    updateLocale("en")
                }
                R.id.rbArabic -> {
                    viewModel.saveLanguage(Language.ARABIC)
                    updateLocale("ar")
                }
            }
            requireActivity().recreate()
        }
        binding.rgLocation.setOnCheckedChangeListener { _, checkedId ->
            if (isListenerEnabled) {

                Log.i("settins", "checkedId: $checkedId")
                when (checkedId) {
                    R.id.rbSettingsGPS -> {
                        checkLocationPermissionAndGps()
                    }
                    R.id.rbSettingsMap -> viewModel.saveLocationStatus(LocationStatus.MAP)
                }
            }
        }
    }



    private fun checkLocationPermissionAndGps() {
        val isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (isLocationPermissionGranted) {
            checkGpsStatusAndFetchLocation()
        } else {
            openPermissionDialog()
        }
    }

    private fun isGpsEnabled(): Boolean {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
    private fun openPermissionDialog() {
        requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun checkGpsStatusAndFetchLocation() {

        if (isGpsEnabled()) {
            fetchCurrentLocationWeather()
        } else {
            showEnableGpsDialog()
        }
    }

    private fun fetchCurrentLocationWeather() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.saveLocationStatus(LocationStatus.GPS)
          fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    viewModel.saveCurrentLocation(latitude, longitude)
                    Log.i("settins", "Latitude: $latitude, Longitude: $longitude")

                } ?: run {
                    Log.e("settins", "Location is null")
                }
            }.addOnFailureListener {
                Log.e("settins", "Failed to get location", it)
            }
        }
    }



    private fun updateLocale(language:String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources: Resources = requireActivity().resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        requireActivity().createConfigurationContext(config)
        this.resources.updateConfiguration(config, requireActivity().resources.displayMetrics)
    }


    private fun showEnableGpsDialog() {
            AlertDialog.Builder(requireActivity()).setMessage(getString(R.string.gps_disabled_message))
                .setPositiveButton(R.string.turn_on_gps) { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }.setNegativeButton(R.string.cancel) { _, _ ->
                cancelGPSPermissionDialog()
            }.setCancelable(false).create().show()

    }


    private fun showGpsPermissionDeniedDialog() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(requireActivity()).setMessage(R.string.gps_permission_required)
                .setPositiveButton(R.string.ok_try_again) { dialog, _ ->
                    openPermissionDialog()
                    dialog.dismiss()
                }.setCancelable(false)
                .setNegativeButton(R.string.cancel) { _, _ ->
                    cancelGPSPermissionDialog()
                }.create().show()
        } else {
            showPermissionDeniedPermanentlyDialog()
        }
    }

    private fun cancelGPSPermissionDialog() {
        isListenerEnabled = false
        if (viewModel.locationStatusFlow.value == LocationStatus.GPS) {
            binding.rgLocation.check(R.id.rbSettingsGPS)
        } else {
            binding.rgLocation.check(R.id.rbSettingsMap)
        }
        isListenerEnabled = true

    }

    private fun showPermissionDeniedPermanentlyDialog() {
        AlertDialog.Builder(requireActivity()).setMessage(R.string.gps_permission_denied_permanently)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    val uri = Uri.fromParts("package", requireActivity().packageName, null)
                    data = uri
                }
                startActivity(intent)
            }.setNegativeButton(R.string.cancel) { _, _ ->
                cancelGPSPermissionDialog()
            }.setCancelable(false).create().show()
    }

    private fun collectSettingsFlows() {
        lifecycleScope.launch {
            viewModel.languageFlow.collect { language ->
                when (language) {
                    Language.ENGLISH -> binding.rgLanguage.check(R.id.rbEnglish)
                    Language.ARABIC -> binding.rgLanguage.check(R.id.rbArabic)
                }
            }
        }


        lifecycleScope.launch {
            viewModel.locationStatusFlow.collect { status ->
                when (status) {
                    LocationStatus.GPS -> binding.rgLocation.check(R.id.rbSettingsGPS)
                    LocationStatus.MAP -> binding.rgLocation.check(R.id.rbSettingsMap)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.temperatureFlow.collect { temperature ->
                when (temperature) {
                    Temperature.CELSIUS -> binding.rgTemperature.check(R.id.rbCelsius)
                    Temperature.FAHRENHEIT -> binding.rgTemperature.check(R.id.rbFahrenheit)
                    Temperature.KELVIN -> binding.rgTemperature.check(R.id.rbKelvin)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.windSpeedFlow.collect { windSpeed ->
                when (windSpeed) {
                    WindSpeed.METERS_PER_SECOND -> binding.rgWindSpeed.check(R.id.rbMeterPerSecond)
                    WindSpeed.MILES_PER_HOUR -> binding.rgWindSpeed.check(R.id.rbMilePerHour)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.notificationStatusFlow.collect { isEnabled ->
                if (isEnabled) {
                    binding.rgNotifications.check(R.id.rbEnable)
                } else {
                    binding.rgNotifications.check(R.id.rbDisable)
                }
            }
        }

    }
}
