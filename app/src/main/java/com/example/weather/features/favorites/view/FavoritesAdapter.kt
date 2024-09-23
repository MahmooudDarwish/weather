package com.example.weather.features.favorites.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.utils.model.Local.WeatherEntity


class FavoritesAdapter(
    private var items: MutableList<WeatherEntity?>,
    private val itemClickListener: IFavoriteItem,
) : RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {

    inner class FavoritesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val locationName: TextView = itemView.findViewById(R.id.locationAddress)
        private val deleteButton: View = itemView.findViewById(R.id.weatherDayIcon)

        fun bind(weatherEntity: WeatherEntity) {
            locationName.text = weatherEntity.name

            deleteButton.setOnClickListener {
                itemClickListener.onDeleteItem(weatherEntity)
            }
            itemView.setOnClickListener {
                itemClickListener.onClickItem(weatherEntity)
            }
        }
    }

    fun updateList(favorites: List<WeatherEntity?>) {
        items.clear()
        items.addAll(favorites)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return FavoritesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        items[position]?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int = items.size

}
