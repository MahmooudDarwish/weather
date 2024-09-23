package com.example.weather.features.favorites.view

import com.example.weather.utils.model.Local.WeatherEntity

interface IFavoriteItem {
   fun onDeleteItem(weatherEntity: WeatherEntity)

   fun onClickItem(weatherEntity: WeatherEntity)


}