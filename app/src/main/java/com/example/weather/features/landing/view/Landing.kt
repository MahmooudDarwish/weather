package com.example.weather.features.landing.view

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.Switch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.weather.R
import com.example.weather.features.landing.view_model.LandingFactory
import com.example.weather.features.landing.view_model.LandingViewModel
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferencesManager
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import com.google.android.material.navigation.NavigationView
import com.example.weather.features.map.view.Map
import com.example.weather.utils.constants.Keys
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import com.example.weather.features.home.view.Home
import com.example.weather.features.home.view.UpdateLocationWeather
import com.example.weather.utils.SharedDataManager
import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.LocationStatus
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale


class LandingActivity : AppCompatActivity() {

    lateinit var viewModel: LandingViewModel
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rbGps: RadioButton
    private lateinit var rbMap: RadioButton
    private lateinit var switchNotifications: Switch
    private lateinit var btnOk: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var gpsStatusReceiver: BroadcastReceiver
    private lateinit var toolbar: Toolbar
    private lateinit var toggle: ActionBarDrawerToggle
    private var snackbar: Snackbar? = null
    private var isGpsStatusReceiverRegistered = false
    private var isOpenLocation = false
    private lateinit var toolbarTitle: TextView

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        lifecycleScope.launch {

            val language = SharedDataManager.languageFlow.first() // Collect the latest value
            Log.i("DEBUGG", "Latest language: $language")
            when (language) {
                Language.ENGLISH -> updateLocale("en")
                Language.ARABIC -> updateLocale("ar")
            }
        }
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

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            showGpsPermissionDeniedDialog()
        }
    }

    private val mapActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val latitude = data?.getDoubleExtra(Keys.LATITUDE_KEY, 0.0) ?: 0.0
            val longitude = data?.getDoubleExtra(Keys.LONGITUDE_KEY, 0.0) ?: 0.0
            Log.d("LandingActivity", "Latitude: $latitude, Longitude: $longitude")
            viewModel.saveCurrentLocation(latitude, longitude)
            updateHomeLocation(latitude, longitude)
        } else {
            showInitialSetupDialog()
        }
    }

    private fun isGpsEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun registerGpsStatusReceiver() {
        gpsStatusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    if (isGpsEnabled()) {
                        // Hide the Snackbar when GPS is enabled
                        hideGPSDisabledSnackBar()
                    } else if (!isGpsEnabled() && viewModel.getLocationStatus() == LocationStatus.GPS) {
                        // Show the Snackbar when GPS is disabled
                        showGPSDisabledSnackBar()
                    }
                }
            }
        }

        val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(gpsStatusReceiver, intentFilter)
        isGpsStatusReceiverRegistered = true
    }


    private fun updateHomeLocation(latitude: Double, longitude: Double) {
        val homeFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val home =
            homeFragment?.childFragmentManager?.fragments?.find { it is Home } as? UpdateLocationWeather
        home?.updateLocation(Pair(latitude, longitude))
    }


    override fun onStart() {
        super.onStart()
        registerGpsStatusReceiver()
    }


    override fun onStop() {
        super.onStop()
        unregisterGpsStatusReceiver()
    }


    private fun unregisterGpsStatusReceiver() {
        if (isGpsStatusReceiverRegistered) {
            unregisterReceiver(gpsStatusReceiver)
            isGpsStatusReceiverRegistered = false
        }
    }


    override fun onResume() {
        Log.i("onResume", "onResume called")
        super.onResume()

        val isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val isGpsLocationStatus = viewModel.getLocationStatus() == LocationStatus.GPS

        if (isLocationPermissionGranted) {
            Log.i("onResume", "Location permission is granted, proceeding")

            if (isGpsEnabled() && isGpsLocationStatus) {
                Log.i("onResume", "GPS is enabled, fetching current location weather")
                fetchCurrentLocationWeather()
            } else {
                Log.i("onResume", "GPS is not enabled")

                if (isGpsLocationStatus) {
                    Log.i("onResume", "Onresume")
                    checkGpsStatusAndFetchLocation()

                } else {
                    Log.i("onResume", "Location status is not GPS, skipping GPS check")
                }
            }
        } else {
            Log.i("onResume", "Location permission is not granted, cannot proceed")
            if(isOpenLocation){
                showPermissionDeniedPermanentlyDialog()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupToolbar()
        setupDrawer()
        setupNavigation()

        val landingFactory = LandingFactory(
            WeatherRepositoryImpl.getInstance(
                remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
                localDataSource = WeatherLocalDataSourceImpl(
                    AppDatabase.getDatabase(this).weatherDao(),
                    AppDatabase.getDatabase(this).alarmDao()
                ),
                sharedPreferences = SharedPreferencesManager(this.getSharedPreferences(Keys.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE))

            )


        )



        viewModel = ViewModelProvider(this, landingFactory).get(LandingViewModel::class.java)
        if (!viewModel.isFirstLaunch()) {
            showInitialSetupDialog()
        }
    }



    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = ""
        toolbarTitle = findViewById(R.id.toolbar_title)
        setSupportActionBar(toolbar)
    }

    private fun setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout)

        // Setup ActionBarDrawerToggle for navigation drawer
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setupNavigation() {
        val navView: NavigationView = findViewById(R.id.navigation_view)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(navView, navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateToolbarTitle(destination)
            toggle.isDrawerIndicatorEnabled = true
            toggle.syncState()
        }
    }

    private fun updateToolbarTitle(destination: NavDestination) {

        toolbarTitle.text = when (destination.id) {
            R.id.nav_home -> getString(R.string.home)
            R.id.nav_favorites -> getString(R.string.favorites)
            R.id.nav_settings -> getString(R.string.settings)
            R.id.nav_alerts -> getString(R.string.alerts)
            else -> getString(R.string.app_name)
        }


    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, drawerLayout) || super.onSupportNavigateUp()
    }


    private fun showInitialSetupDialog() {
        val dialogView = layoutInflater.inflate(R.layout.landing_dialog, null)

        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()

        initDialogUI(dialogView)
        setUpDialogListeners(dialog)

        dialog.show()
    }

    private fun initDialogUI(dialogView: View) {
        rbGps = dialogView.findViewById(R.id.rbDialogGps)
        rbMap = dialogView.findViewById(R.id.rbDialogMAP)
        switchNotifications = dialogView.findViewById(R.id.switchNotificationsSwitch)
        btnOk = dialogView.findViewById(R.id.btnOk)

    }

    private fun setUpDialogListeners(dialog: AlertDialog) {
        btnOk.setOnClickListener {
            when {
                rbGps.isChecked -> {
                    viewModel.saveLocationStatus(LocationStatus.GPS)
                    requestLocationPermission()
                }

                rbMap.isChecked -> {
                    viewModel.saveLocationStatus(LocationStatus.MAP)
                    navigateToMaps()
                }
            }
            when {
                switchNotifications.isChecked -> viewModel.saveNotificationStatus(true)
                switchNotifications.isChecked.not() -> viewModel.saveNotificationStatus(false)
            }
            viewModel.setFirstLaunchCompleted()
            dialog.dismiss()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            checkGpsStatusAndFetchLocation()
        } else {
            openPermissionDialog()
        }
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
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                600000
            ).apply {
                setMinUpdateIntervalMillis(600000)
                setGranularity(Granularity.GRANULARITY_FINE)
            }.build()

            fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    p0.let {

                        val location = it.lastLocation
                        if (location != null) {
                            Log.i("LandingActivity", "long: ${location.longitude}, lat: ${location.longitude}")
                            val latitude = location.latitude
                            val longitude = location.longitude
                            viewModel.saveCurrentLocation(latitude, longitude)
                            updateHomeLocation(latitude, longitude)
                        }
                    }
                }
            }, Looper.getMainLooper())
        }
    }

    //the user choose the gps option from the dialog
    private fun showEnableGpsDialog() {
        if (viewModel.getLocationStatus() == LocationStatus.GPS && viewModel.getCurrentLocation() == null) {
            AlertDialog.Builder(this).setMessage(getString(R.string.gps_disabled_message))
                .setPositiveButton(R.string.turn_on_gps) { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }.setNegativeButton(R.string.continue_with_specific_location) { _, _ ->
                    navigateToMaps()
                }.setCancelable(false).create().show()
        }
    }

    private fun showGPSDisabledSnackBar() {
        val rootView = findViewById<View>(android.R.id.content)
        snackbar = Snackbar.make(rootView, R.string.gps_disabled_snackbar, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.turn_on_gps) {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setBackgroundTint(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            .setTextColor(ContextCompat.getColor(this, android.R.color.white))
            .setActionTextColor(ContextCompat.getColor(this, android.R.color.white))
        snackbar?.show()
    }

    private fun hideGPSDisabledSnackBar() {
        snackbar?.dismiss()
    }

    private fun showGpsPermissionDeniedDialog() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // If the user hasn't permanently denied the permission, we can show the dialog again.
            AlertDialog.Builder(this).setMessage(R.string.gps_permission_required)
                .setPositiveButton(R.string.ok_try_again) { dialog, _ ->
                    openPermissionDialog()
                    dialog.dismiss()
                }.setCancelable(false)
                .setNegativeButton(R.string.continue_with_specific_location) { _, _ ->
                    navigateToMaps()
                }.create().show()
        } else {
            showPermissionDeniedPermanentlyDialog()
        }
    }

    private fun showPermissionDeniedPermanentlyDialog() {
        AlertDialog.Builder(this).setMessage(R.string.gps_permission_denied_permanently)
            .setPositiveButton(R.string.open_settings) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    isOpenLocation = true
                    val uri = Uri.fromParts("package", packageName, null)
                    data = uri
                }
                startActivity(intent)
            }.setNegativeButton(R.string.continue_with_specific_location) { _, _ ->
                navigateToMaps()

            }.setCancelable(false).create().show()
    }

    private fun navigateToMaps() {
        viewModel.saveLocationStatus(LocationStatus.MAP)
        val intent = Intent(this, Map::class.java)
        mapActivityResultLauncher.launch(intent)
    }

}

