package com.example.weather.utils.shared_adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.utils.Utils
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.model.Local.HourlyWeatherEntity

class HourlyWeatherAdapter(
    private var weatherList: List<HourlyWeatherEntity?>,
    private val temperatureUnit: Temperature,
    private val context: Context

) :
    RecyclerView.Adapter<HourlyWeatherAdapter.WeatherViewHolder>() {

    class WeatherViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val time: TextView = view.findViewById(R.id.weatherHourlyTime)
        val temp: TextView = view.findViewById(R.id.weatherHourlyTemperature)
        val icon: ImageView = view.findViewById(R.id.weatherHourlyIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weather_hourly, parent, false)
        return WeatherViewHolder(view)
    }

    fun updateHourlyWeatherList(filteredList : List<HourlyWeatherEntity?>){
        weatherList = filteredList
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val weatherItem = weatherList[position] ?: return
        val convertedTemp = Utils().getWeatherMeasure(weatherItem.temp.toInt(), temperatureUnit)

        holder.time.text = weatherItem.dt_txt.substring(11, 16)
        holder.temp.text = context.getString(R.string.temperature_format, convertedTemp,Utils().getUnitSymbol(temperatureUnit ))


        holder.icon.setImageResource(Utils().getWeatherIcon(weatherItem.icon))

    }



    override fun getItemCount(): Int {
        return minOf(weatherList.size, 24)
    }
}
