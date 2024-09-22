package com.example.weather.utils

import android.content.Context
import androidx.core.content.ContextCompat.getString
import java.text.SimpleDateFormat
import java.util.*
import com.example.weather.R
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.enums.WindSpeed
class Utils {

    fun getDayNameFromEpoch(context: Context,epochTime: Long): String {
        val date = Date(epochTime * 1000)

        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        val dayName = sdf.format(date)

        val calendar = Calendar.getInstance()
        val inputDateCalendar = Calendar.getInstance().apply { time = date }

        val isToday = calendar.get(Calendar.YEAR) == inputDateCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.MONTH) == inputDateCalendar.get(Calendar.MONTH) &&
                calendar.get(Calendar.DAY_OF_MONTH) == inputDateCalendar.get(Calendar.DAY_OF_MONTH)

        val tomorrowCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val isTomorrow =
            tomorrowCalendar.get(Calendar.YEAR) == inputDateCalendar.get(Calendar.YEAR) &&
                    tomorrowCalendar.get(Calendar.MONTH) == inputDateCalendar.get(Calendar.MONTH) &&
                    tomorrowCalendar.get(Calendar.DAY_OF_MONTH) == inputDateCalendar.get(Calendar.DAY_OF_MONTH)

        return when {
            isToday -> getString(context ,R.string.today)
            isTomorrow -> getString(context ,R.string.tomorrow)
            else -> dayName
        }
    }

    fun getWeatherIcon(imageName: String): Int {
        when (imageName) {
            "01d" -> return R.drawable.ic_sunny
            "01n" -> return R.drawable.ic_night
            "02d" -> return R.drawable.ic_partly_cloudy
            "02n" -> return R.drawable.ic_night_with_cloud
            "03d" -> return R.drawable.ic_cloudy
            "03n" -> return R.drawable.ic_cloudy
            "04d" -> return R.drawable.ic_mostly_cloudy
            "04n" -> return R.drawable.ic_night_mostly_cloudy
            "09d" -> return R.drawable.ic_rain
            "09n" -> return R.drawable.ic_rain
            "10d" -> return R.drawable.ic_sunny_rain
            "10n" -> return R.drawable.ic_night_rain
            "11d" -> return R.drawable.ic_heavy_thunderstorm
            "11n" -> return R.drawable.ic_heavy_thunderstorm
            "13d" -> return R.drawable.ic_snow
            "13n" -> return R.drawable.ic_snow
            "50d" -> return R.drawable.ic_fog
            "50n" -> return R.drawable.ic_fog
            else -> return R.drawable.ic_partly_cloudy

        }
    }

    fun isSameDay(hourlyEpoch: Long, dayEpoch: Long): Boolean {
        val hourlyDate = Date(hourlyEpoch * 1000)
        val dayDate = Date(dayEpoch * 1000)

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return dateFormat.format(hourlyDate) == dateFormat.format(dayDate)
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    fun getWeatherMeasure(value: Int, toMeasure: Temperature): Double {
        return when (toMeasure) {
            Temperature.FAHRENHEIT -> (value * 9.0 / 5.0)+32.0
            Temperature.KELVIN -> value+273.15
            Temperature.CELSIUS -> value.toDouble()

        }
    }

    fun getUnitSymbol(temperature: Temperature): String {
        return when (temperature) {
            Temperature.FAHRENHEIT -> "°F"
            Temperature.KELVIN -> "°K"
            Temperature.CELSIUS -> "°C"
        }
    }

    fun metersPerSecondToMilesPerHour(mps: Double, windSpeed: WindSpeed): Double {
        return when (windSpeed) {
            WindSpeed.MILES_PER_HOUR -> (mps * 2.23694)
            WindSpeed.METERS_PER_SECOND -> mps
        }
    }
    fun getSpeedUnitSymbol(windSpeed: WindSpeed, context: Context): String {
        return when (windSpeed) {
            WindSpeed.METERS_PER_SECOND -> getString(context, R.string.wind_speed_mps)
            WindSpeed.MILES_PER_HOUR -> getString(context, R.string.wind_speed_mph)
        }
    }
}