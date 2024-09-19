package com.example.weather.features.landing.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
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
import com.example.weather.features.home.view.Home
import com.example.weather.features.home.view.UpdateLocationWeather
import com.example.weather.features.landing.view_model.LandingFactory
import com.example.weather.features.landing.view_model.LandingViewModel
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferences
import com.example.weather.utils.model.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import com.google.android.material.navigation.NavigationView
import com.example.weather.features.map.view.Map



class LandingActivity : AppCompatActivity() {

    lateinit var viewModel: LandingViewModel
    private lateinit var drawerLayout: DrawerLayout


    private val mapActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val latitude = data?.getDoubleExtra("LATITUDE", 0.0) ?: 0.0
            val longitude = data?.getDoubleExtra("LONGITUDE", 0.0) ?: 0.0

            // Pass the data to the Home fragment
            val homeFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
            val home = homeFragment?.childFragmentManager?.fragments?.find { it is Home } as? UpdateLocationWeather
            home?.updateLocation(latitude, longitude)
        }
    }

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            checkGpsStatusAndFetchLocation()
        } else {
            showGpsPermissionDeniedDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.navigation_view)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        setupActionBarWithNavController(navController, drawerLayout)
        NavigationUI.setupWithNavController(navView, navController)


        val landingFactory = LandingFactory(
            WeatherRepositoryImpl.getInstance(
                remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
                localDataSource = WeatherLocalDataSourceImpl(AppDatabase.getDatabase(this).weatherDao(), AppDatabase.getDatabase(this).forecastDao()),
                sharedPreferences = SharedPreferences(this)

            )
        )

        viewModel = ViewModelProvider(this, landingFactory).get(LandingViewModel::class.java)
        showInitialSetupDialog()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
                || super.onSupportNavigateUp()
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun showInitialSetupDialog() {
        val dialogView = layoutInflater.inflate(R.layout.landing_dialog, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val rbGps = dialogView.findViewById<RadioButton>(R.id.rbGps)
        val rbMap = dialogView.findViewById<RadioButton>(R.id.rbMap)
        val switchNotifications = dialogView.findViewById<Switch>(R.id.switchNotificationsSwitch)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        btnOk.setOnClickListener {
            when {
                rbGps.isChecked -> {
                    viewModel.selectGps(true)
                    requestLocationPermission()
                }

                rbMap.isChecked -> {
                    viewModel.selectMap(true)
                    navigateToMaps()
                }
            }
            viewModel.setNotificationsEnabled(switchNotifications.isChecked)
            dialog.dismiss()
        }

        dialog.show()
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

    private fun  openPermissionDialog(){
        requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun checkGpsStatusAndFetchLocation() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!isGpsEnabled) {
            showEnableGpsDialog()
        }
    }

    private fun showEnableGpsDialog() {
        AlertDialog.Builder(this)
            .setMessage("GPS is turned off. Please turn it on to fetch your location.")
            .setPositiveButton("Turn on") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }.setNegativeButton("Continue with specific location") { _, _ ->
                navigateToMaps()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun showGpsPermissionDeniedDialog() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // If the user hasn't permanently denied the permission, we can show the dialog again.
            AlertDialog.Builder(this)
                .setMessage("Location permission is required to fetch weather data using GPS.")
                .setPositiveButton("OK") { dialog, _ ->
                    openPermissionDialog()
                    dialog.dismiss()
                }
                .setCancelable(false)
                .setNegativeButton("Continue with specific location") { _, _ ->
                    navigateToMaps()
                }
                .create()
                .show()
        } else {
            showPermissionDeniedPermanentlyDialog()
        }
    }

    private fun showPermissionDeniedPermanentlyDialog() {
        AlertDialog.Builder(this)
            .setMessage("Location permission is permanently denied. You need to enable it from the app settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    val uri = Uri.fromParts("package", packageName, null)
                    data = uri
                }
                startActivity(intent)
            }
            .setNegativeButton("Continue with specific location") { _, _ ->
                navigateToMaps()
            }       .create()
            .show()
            }

    private fun navigateToMaps() {
        val intent = Intent(this, Map::class.java)
        mapActivityResultLauncher.launch(intent)
    }

}

