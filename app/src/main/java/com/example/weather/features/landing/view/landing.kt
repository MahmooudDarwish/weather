package com.example.weather.features.landing.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.RadioButton
import android.widget.Switch
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.weather.MapActivity
import com.example.weather.R
import com.example.weather.features.landing.view_model.LandingViewModel

class LandingActivity : AppCompatActivity() {

    private val viewModel: LandingViewModel by viewModels()

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
        setContentView(R.layout.landing_activity)

        showInitialSetupDialog()
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

        viewModel.isGpsSelected.observe(this) { isSelected ->
            rbGps.isChecked = isSelected
        }

        viewModel.isMapSelected.observe(this) { isSelected ->
            rbMap.isChecked = isSelected
        }

        viewModel.isNotificationsEnabled.observe(this) { isEnabled ->
            switchNotifications.isChecked = isEnabled
        }

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
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun checkGpsStatusAndFetchLocation() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (isGpsEnabled) {
            // GPS is enabled, fetch location
            fetchGpsLocation()
        } else {
            // Prompt user to enable GPS
            showEnableGpsDialog()
        }
    }

    private fun showEnableGpsDialog() {
        AlertDialog.Builder(this)
            .setMessage("GPS is turned off. Please turn it on to fetch your location.")
            .setPositiveButton("Turn on") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun showGpsPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setMessage("Location permission is required to fetch weather data using GPS.")
            .setPositiveButton("OK", null).setCancelable(false).setOnDismissListener { }
            .create()
            .show()
    }

    private fun fetchGpsLocation() {
        // Logic to fetch GPS location (can be done via a ViewModel or directly)
        // This method would interact with your WeatherRepository to fetch the weather based on location
    }

    private fun navigateToMaps() {
        // Navigate to the Maps activity for location selection
        startActivity(Intent(this, MapActivity::class.java))
    }
}
