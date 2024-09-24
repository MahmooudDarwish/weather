package com.example.weather.features.weather_deatils.view


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.utils.Utils
import com.example.weather.utils.enums.Temperature
import com.example.weather.utils.model.ForecastItem
import com.example.weather.utils.model.Local.HourlyWeatherEntity

class FavoriteHourlyWeatherAdapter(
    private var weatherList: List<HourlyWeatherEntity?>,
    private val temperatureUnit: Temperature

) :
    RecyclerView.Adapter<FavoriteHourlyWeatherAdapter.WeatherViewHolder>() {

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
        val weatherItem = weatherList.get(position)
        val convertedTemp = Utils().getWeatherMeasure(weatherItem?.temp!!.toInt(), temperatureUnit)

        holder.time.text = weatherItem.dt_txt.substring(11, 16)
        holder.temp.text = "${convertedTemp.toInt()}${Utils().getUnitSymbol(temperatureUnit)}"


        holder.icon.setImageResource(Utils().getWeatherIcon(weatherItem.icon))

    }



    override fun getItemCount(): Int {
        return minOf(weatherList.size, 24)
    }
}
