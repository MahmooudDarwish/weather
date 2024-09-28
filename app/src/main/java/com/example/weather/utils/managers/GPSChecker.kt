package com.example.weather.utils.managers

import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class GPSChecker(private val context: Context) {

    private val _gpsStateFlow = MutableSharedFlow<Boolean>(replay = 1)
    val gpsStateFlow: SharedFlow<Boolean> = _gpsStateFlow

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            checkLocationEnabled()
        }
    }

    fun startMonitoring() {
        val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        context.registerReceiver(locationReceiver, intentFilter)
        checkLocationEnabled()
    }

    fun stopMonitoring() {
        context.unregisterReceiver(locationReceiver)
    }

    private fun checkLocationEnabled() {
        val isLocationEnabled = isLocationEnabled()
        CoroutineScope(Dispatchers.Default).launch {
            _gpsStateFlow.emit(isLocationEnabled)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        return gpsEnabled || networkEnabled
    }
}
