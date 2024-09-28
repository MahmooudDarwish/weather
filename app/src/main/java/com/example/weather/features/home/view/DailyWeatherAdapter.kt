package com.example.weather.features.home.view

import android.content.Context
import android.graphics.Color
import android.util.Log


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.utils.Utils
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.model.Local.DailyWeatherEntity
import java.util.Locale


class DailyWeatherAdapter(
    private var weatherList: List<DailyWeatherEntity?>,
    private val onDayClickListener: OnDayClickListener,
    private val temperatureUnit: Temperature,
    private val context: Context
) : RecyclerView.Adapter<DailyWeatherAdapter.DayViewHolder>() {

    private var selectedPosition: Int = 0

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val weatherDay: TextView = view.findViewById(R.id.weatherDay)
        val tempMinMax: TextView = view.findViewById(R.id.tempMinMax)
        val icon: ImageView = view.findViewById(R.id.weatherDayIcon)
        val tempDesc: TextView = view.findViewById(R.id.weatherDayDesc)
        val card: CardView = view.findViewById(R.id.weatherDayCard)
    }

    fun updateData(newList: List<DailyWeatherEntity?>) {
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
        val weatherItem = weatherList[position] ?: return

        holder.weatherDay.text = Utils().getDayNameFromEpoch(context = context, epochTime =  weatherItem.dt)
        holder.icon.setImageResource(Utils().getWeatherIcon(weatherItem.icon))
        holder.tempDesc.text = weatherItem.description.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
        }

        val maxTempInCelsius = weatherItem.maxTemp.toInt()
        val minTempInCelsius = weatherItem.minTemp.toInt()
        val convertedMaxTemp = Utils().getWeatherMeasure(maxTempInCelsius, temperatureUnit)
        val convertedMinTemp = Utils().getWeatherMeasure(minTempInCelsius, temperatureUnit)
        val unitSymbol = Utils().getUnitSymbol(temperatureUnit)

        val formattedTemp = context.getString(R.string.temp_min_max_format,
            convertedMaxTemp.toInt(),
            convertedMinTemp.toInt(),
            unitSymbol)

        holder.tempMinMax.text = formattedTemp

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
