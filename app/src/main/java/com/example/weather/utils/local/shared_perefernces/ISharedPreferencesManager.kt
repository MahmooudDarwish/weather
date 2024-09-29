package com.example.weather.utils.local.shared_perefernces

import com.example.weather.utils.enums.Language
import com.example.weather.utils.enums.LocationStatus
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed

interface ISharedPreferencesManager {

    fun setFirstLaunchCompleted(status: Boolean)
    fun isFirstLaunch(): Boolean

    fun setLanguage(lang: Language)
    fun getLanguage(): Language

    fun setTemperatureUnit(unit: Temperature)
    fun getTemperatureUnit(): Temperature

    fun setWindSpeedUnit(unit: WindSpeed)
    fun getWindSpeedUnit(): WindSpeed

    fun setLocationStatus(locationStatus: LocationStatus)
    fun getLocationStatus(): LocationStatus

    fun setNotificationStatus(status: Boolean)
    fun getNotificationStatus(): Boolean

    fun setLocation(latitude: Double, longitude: Double)
    fun getLocation(): Pair<Double, Double>?
}
