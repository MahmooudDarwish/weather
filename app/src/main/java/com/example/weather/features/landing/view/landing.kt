package com.example.weather.features.landing.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.Switch
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.LayoutInflater
import androidx.lifecycle.Observer
import com.example.weather.R
import com.example.weather.features.landing.view_model.LandingViewModel

class LandingActivity : AppCompatActivity() {

    // Lazy initialization of ViewModel using Kotlin's viewModels() delegate
    private val viewModel: LandingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showInitialSetupDialog()
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun showInitialSetupDialog() {
        // Inflate the custom layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.landing_dialog, null)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Prevent dismissing without clicking OK
            .create()

        // Get views from the dialog layout
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

        // Handle OK button click
        btnOk.setOnClickListener {
            viewModel.selectGps(rbGps.isChecked)
            viewModel.selectMap(rbMap.isChecked)
            viewModel.setNotificationsEnabled(switchNotifications.isChecked)


            dialog.dismiss()
        }

        dialog.show()
    }
}
