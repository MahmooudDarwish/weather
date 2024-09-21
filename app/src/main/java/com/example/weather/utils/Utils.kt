package com.example.weather.utils

import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import com.example.weather.R

 class Utils{

     fun getDayNameFromEpoch(epochTime: Long): String {
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
         val isTomorrow = tomorrowCalendar.get(Calendar.YEAR) == inputDateCalendar.get(Calendar.YEAR) &&
                 tomorrowCalendar.get(Calendar.MONTH) == inputDateCalendar.get(Calendar.MONTH) &&
                 tomorrowCalendar.get(Calendar.DAY_OF_MONTH) == inputDateCalendar.get(Calendar.DAY_OF_MONTH)

         return when {
             isToday -> "Today"
             isTomorrow -> "Tomorrow"
             else -> dayName
         }
     }

      fun getWeatherIcon(imageName: String) : Int {
         Log.d("Utils", "getWeatherIcon: $imageName")
         when(imageName){
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
}