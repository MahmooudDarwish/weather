package com.example.weather.features.home.view

import com.example.weather.utils.model.ForecastResponse


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.utils.Utils
import java.util.Locale


class DailyWeatherAdapter(private val weatherList: ForecastResponse,) :
    RecyclerView.Adapter<DailyWeatherAdapter.DayViewHolder>() {

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val weatherDay: TextView = view.findViewById(R.id.weatherDay)
        val tempMinMax: TextView = view.findViewById(R.id.tempMinMax)
        val icon: ImageView = view.findViewById(R.id.weatherDayIcon)
        val tempDesc: TextView = view.findViewById(R.id.weatherDayDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weather_daily, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val weatherItem = weatherList.list[position]



        holder.weatherDay.text = Utils().getDayNameFromEpoch(weatherItem.dt)
        holder.icon.setImageResource(Utils().getWeatherIcon(weatherItem.weather[0].icon))
        holder.tempDesc.text = weatherItem.weather[0].description.
        replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.ROOT
            ) else it.toString()
        }
        holder.tempMinMax.text = "${weatherItem.temp.max.toInt()}/${weatherItem.temp.min.toInt()}°C"
    }

    override fun getItemCount(): Int {
        return weatherList.list.size
    }
}
