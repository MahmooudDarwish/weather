package com.example.weather.features.settings.view


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class Settings : Fragment() {


    private lateinit var viewModel: SettingsViewModel
    private lateinit var factory: SettingsViewModelFactory
    private lateinit var radioGroupTemperature: RadioGroup
    private lateinit var radioGroupWindSpeed: RadioGroup
    private lateinit var radioGroupNotification: RadioGroup
    private lateinit var radioGroupLanguage: RadioGroup
    private lateinit var radioGroupLocation: RadioGroup
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

    override fun onResume() {
        super.onResume()

        Log.i("DEBUGGGGGGG", "viewModel.settings() ${viewModel.getLocationStatus()}")


        val isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (isLocationPermissionGranted) {
            if (isGpsEnabled()) {
                fetchCurrentLocationWeather()
            }else{
                if (viewModel.getLocationStatus() == LocationStatus.GPS) {
                    checkGpsStatusAndFetchLocation()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        factory = SettingsViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
                localDataSource = WeatherLocalDataSourceImpl(
                    AppDatabase.getDatabase(requireActivity()).weatherDao(),
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
            if (isListenerEnabled) {

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
            // If the user hasn't permanently denied the permission, we can show the dialog again.
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
        //Use flag to not trigger the listener while change the value
        isListenerEnabled = false
        radioGroupLocation.check(R.id.rbSettingsMap)
        viewModel.saveLocationStatus(LocationStatus.MAP)
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

        when (viewModel.getLocationStatus()) {
            LocationStatus.GPS -> radioGroupLocation.check(R.id.rbSettingsGPS)
            LocationStatus.MAP -> radioGroupLocation.check(R.id.rbSettingsMap)
        }
    }



}
