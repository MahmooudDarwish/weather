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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.features.favorites.view_model.FavoritesViewModel
import com.example.weather.features.favorites.view_model.FavoritesViewModelFactory
import com.example.weather.features.map.view.Map
import com.example.weather.utils.constants.Keys
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferences
import com.example.weather.utils.model.Local.WeatherEntity
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Favorites : Fragment() , IFavoriteItem{

    private lateinit var  viewModel: FavoritesViewModel
    private lateinit var  favoritesIcon : ImageView
    private lateinit var  addFavoritesFAB : FloatingActionButton
    private lateinit var  noFavoritesTextView : TextView
    private lateinit var  favoritesRecyclerView : RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter

    private val mapActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val latitude = data?.getDoubleExtra(Keys.LATITUDE_KEY, 0.0) ?: 0.0
            val longitude = data?.getDoubleExtra(Keys.LONGITUDE_KEY, 0.0) ?: 0.0
            val city = data?.getStringExtra(Keys.CITY_KEY) ?: ""

            Log.d("HomeActivity", "Latitude: $latitude, Longitude: $longitude")
            saveFavoriteLocation(Pair(latitude, longitude), city)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val favoriteFactory = FavoritesViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
                localDataSource = WeatherLocalDataSourceImpl(
                    AppDatabase.getDatabase(requireActivity()).weatherDao(),
                ),
                sharedPreferences = SharedPreferences(requireActivity())

            )
        )

        viewModel = ViewModelProvider(this, favoriteFactory).get(FavoritesViewModel::class.java)

        viewModel.fetchAllFavoriteWeather()

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
        observeFavorites()

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


        favoritesAdapter = FavoritesAdapter(mutableListOf(), this)
        favoritesRecyclerView.adapter = favoritesAdapter



    }

    private fun observeFavorites() {
        viewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            favoritesAdapter.updateList(favorites)
            observeNoFavorites(favorites.isNotEmpty())
        }
    }

    private fun observeNoFavorites(favoriteExist: Boolean){
        if(favoriteExist){
            Log.d("Favorites", "Favorites exist")
            noFavoritesTextView.visibility = View.GONE
            favoritesIcon.visibility =  View.GONE
        }else{
            Log.d("Favorites", "Favorites not exist")
            noFavoritesTextView.visibility = View.VISIBLE
            favoritesIcon.visibility =  View.VISIBLE

        }

    }

    private fun navigateToMaps() {
        mapActivityResultLauncher.launch(Intent(requireActivity(), Map::class.java))
    }

    private fun saveFavoriteLocation(location: Pair<Double, Double>,  city : String) {
        viewModel.getWeatherAndSaveToDatabase(location.first, location.second, city)
    }
    override fun onDeleteItem(weatherEntity: WeatherEntity) {
        viewModel.deleteFavorite(weatherEntity)
    }

    override fun onClickItem(weatherEntity: WeatherEntity) {

    }

}