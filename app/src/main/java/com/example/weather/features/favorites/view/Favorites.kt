package com.example.weather.features.favorites.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.features.favorites.view_model.FavoritesViewModel
import com.example.weather.features.home.view.HourlyWeatherAdapter
import com.example.weather.features.map.view.Map
import com.example.weather.utils.constants.Keys
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Favorites : Fragment() , IFavoriteItem{

    private val viewModel: FavoritesViewModel by viewModels()
    private lateinit var  favoritesIcon : ImageView
    private lateinit var  addFavoritesFAB : FloatingActionButton
    private lateinit var  noFavoritesTextView : TextView
    private lateinit var  favoritesRecyclerView : RecyclerView

    private val mapActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val latitude = data?.getDoubleExtra(Keys.LATITUDE_KEY, 0.0) ?: 0.0
            val longitude = data?.getDoubleExtra(Keys.LONGITUDE_KEY, 0.0) ?: 0.0
            Log.d("HomeActivity", "Latitude: $latitude, Longitude: $longitude")
           // viewModel.saveCurrentLocation(latitude, longitude)
           // updateLocation(Pair(latitude, longitude))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initUi(view)
        setUpListeners()


    }

    private fun setUpListeners() {
        addFavoritesFAB.setOnClickListener {
            navigateToMaps()
        }
    }

    private fun initUi(view: View) {
        favoritesIcon = view.findViewById(R.id.favoriteIcon)
        noFavoritesTextView = view.findViewById(R.id.noFavoritesText)
        addFavoritesFAB = view.findViewById(R.id.addFavoriteLocationFAB)
        favoritesRecyclerView = view.findViewById(R.id.favoritesRecyclerView)
        favoritesRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)


        //hourlyWeatherAdapter = HourlyWeatherAdapter(emptyList(), viewModel.getWeatherMeasure())
        //recyclerViewHourlyWeather.adapter = hourlyWeatherAdapter

    }

    fun observeNoFavorites(favoriteExist: Boolean){
        if(favoriteExist){
            noFavoritesTextView.visibility = View.GONE
            favoritesIcon.visibility =  View.GONE
        }else{
            noFavoritesTextView.visibility = View.VISIBLE
            favoritesIcon.visibility =  View.VISIBLE
        }

    }

    private fun navigateToMaps() {
        mapActivityResultLauncher.launch(Intent(requireActivity(), Map::class.java))
    }

    override fun onDeleteItem() {

    }

    override fun onClickItem() {

    }

}