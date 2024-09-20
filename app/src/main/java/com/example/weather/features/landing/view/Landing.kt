package com.example.weather.features.landing.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.weather.R
import com.example.weather.features.landing.view_model.LandingFactory
import com.example.weather.features.landing.view_model.LandingViewModel
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferences
import com.example.weather.utils.model.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import com.google.android.material.navigation.NavigationView
import com.example.weather.features.map.view.Map
import com.example.weather.utils.constants.Keys
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import android.location.Location
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import com.example.weather.utils.enums.LocationStatus
import com.google.android.material.snackbar.Snackbar


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


    private val mapActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val latitude = data?.getDoubleExtra(Keys.LATITUDE_KEY, 0.0) ?: 0.0
            val longitude = data?.getDoubleExtra(Keys.LONGITUDE_KEY, 0.0) ?: 0.0
            Log.d("LandingActivity", "Latitude: $latitude, Longitude: $longitude")
            viewModel.saveCurrentLocation(latitude, longitude)
            //updateHomeLocation(latitude, longitude)
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
                    if (!isGpsEnabled()) {
                        showGPSDisabledSnackBar()
                    }
                }
            }
        }

        val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(gpsStatusReceiver, intentFilter)
    }

    /*
    private fun updateHomeLocation(latitude: Double, longitude: Double) {
        val homeFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val home = homeFragment?.childFragmentManager?.fragments?.find { it is Home } as? UpdateLocationWeather
        home?.updateLocation(latitude, longitude)
    }
        */
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            checkGpsStatusAndFetchLocation()
        } else {
            showGpsPermissionDeniedDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        registerGpsStatusReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(gpsStatusReceiver)
    }

    override fun onResume() {
        Log.i("DEBUGGGGGGG", "onResume")
        super.onResume()

        val isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val isGpsLocationStatus = viewModel.getLocationStatus() == LocationStatus.GPS
        Log.i("DEBUGGGGGGG", "isGpsLocationStatus: $isGpsLocationStatus")
        if (isLocationPermissionGranted) {
            if (isGpsEnabled()) {
                fetchCurrentLocationWeather()
            } else {
                if (isGpsLocationStatus) {
                    checkGpsStatusAndFetchLocation()
                }
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
                    AppDatabase.getDatabase(this).forecastDao()
                ),
                sharedPreferences = SharedPreferences(this)

            )
        )


        viewModel = ViewModelProvider(this, landingFactory).get(LandingViewModel::class.java)
        if (!viewModel.isFirstLaunch()) {
            showInitialSetupDialog()
        }
    }

    private fun setupToolbar() {
         toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    private fun setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout)

        // Setup ActionBarDrawerToggle for navigation drawer
        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
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

        navController.addOnDestinationChangedListener { _, _, _ ->
            toggle.isDrawerIndicatorEnabled = true
            toggle.syncState()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, drawerLayout) || super.onSupportNavigateUp()
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun showInitialSetupDialog() {
        val dialogView = layoutInflater.inflate(R.layout.landing_dialog, null)

        val dialog = AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create()

        initDialogUI(dialogView)
        setUpDialogListeners(dialog)

        dialog.show()
    }

    private fun initDialogUI(dialogView: View) {
        rbGps = dialogView.findViewById(R.id.rbGps)
        rbMap = dialogView.findViewById(R.id.rbMAP)
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
            val locationTask: Task<Location> = fusedLocationClient.lastLocation
            locationTask.addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    viewModel.saveCurrentLocation(latitude, longitude)
                    Log.i("DEBUGGGGGGG", "Latitude: $latitude, Longitude: $longitude")

                } ?: run {
                    Log.e("DEBUGGGGGGG", "Location is null")
                }
            }.addOnFailureListener {
                Log.e("DEBUGGGGGGG", "Failed to get location", it)
            }
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
        Snackbar.make(rootView, R.string.gps_disabled_snackbar, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.turn_on_gps) {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }.setBackgroundTint(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            .setTextColor(ContextCompat.getColor(this, android.R.color.white))
            .setActionTextColor(ContextCompat.getColor(this, android.R.color.white)).show()
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

