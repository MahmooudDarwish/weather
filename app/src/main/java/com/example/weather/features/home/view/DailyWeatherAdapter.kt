package com.example.weather.features.home.view

import android.graphics.Color
import android.util.Log
import com.example.weather.utils.model.ForecastResponse


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.utils.Utils
import com.example.weather.utils.model.DailyForecastItem
import java.util.Locale


class DailyWeatherAdapter(
    private var weatherList: List<DailyForecastItem>,
    private val onDayClickListener: OnDayClickListener
) : RecyclerView.Adapter<DailyWeatherAdapter.DayViewHolder>() {

    private var selectedPosition: Int = 0

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val weatherDay: TextView = view.findViewById(R.id.weatherDay)
        val tempMinMax: TextView = view.findViewById(R.id.tempMinMax)
        val icon: ImageView = view.findViewById(R.id.weatherDayIcon)
        val tempDesc: TextView = view.findViewById(R.id.weatherDayDesc)
        val card : CardView = view.findViewById(R.id.weatherDayCard)
    }

    fun updateData(newList: List<DailyForecastItem>) {
        Log.d("DailyWeatherAdapter", "Updating data: $newList")
        weatherList = newList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weather_daily, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val weatherItem = weatherList[position]

        holder.weatherDay.text = Utils().getDayNameFromEpoch(weatherItem.dt)
        holder.icon.setImageResource(Utils().getWeatherIcon(weatherItem.weather[0].icon))
        holder.tempDesc.text = weatherItem.weather[0].description.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }
        holder.tempMinMax.text = "${weatherItem.temp.max.toInt()}/${weatherItem.temp.min.toInt()}°C"


        if (position == selectedPosition) {
            holder.card.setCardBackgroundColor(Color.parseColor("#ffc107"))
        } else {
            holder.card.setCardBackgroundColor(Color.parseColor("#333333"))
        }

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onDayClickListener.onDayClick(weatherItem)

        }
    }

    override fun getItemCount(): Int {
        Log.d("DailyWeatherAdapter", "getItemCount: ${weatherList.size}")
        return weatherList.size
    }
}
