package com.example.weather.features.favorites.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.databinding.FragmentFavoritesBinding
import com.example.weather.features.favorites.view_model.FavoritesViewModel
import com.example.weather.features.favorites.view_model.FavoritesViewModelFactory
import com.example.weather.features.map.view.Map
import com.example.weather.features.weather_deatils.view.FavoriteDetails
import com.example.weather.utils.constants.Keys
import com.example.weather.utils.local.room.AppDatabase
import com.example.weather.utils.local.room.local_data_source.WeatherLocalDataSourceImpl
import com.example.weather.utils.local.shared_perefernces.SharedPreferencesManager
import com.example.weather.utils.managers.InternetChecker
import com.example.weather.utils.model.DataState
import com.example.weather.utils.model.Local.WeatherEntity
import com.example.weather.utils.model.repository.WeatherRepositoryImpl
import com.example.weather.utils.remote.WeatherRemoteDataSourceImpl
import kotlinx.coroutines.launch

class Favorites : Fragment(), IFavoriteItem {

    private lateinit var binding: FragmentFavoritesBinding

    private lateinit var viewModel: FavoritesViewModel
    private lateinit var favoritesAdapter: FavoritesAdapter

    private val mapActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val latitude = data?.getDoubleExtra(Keys.LATITUDE_KEY, 0.0) ?: 0.0
            val longitude = data?.getDoubleExtra(Keys.LONGITUDE_KEY, 0.0) ?: 0.0
            val city = data?.getStringExtra(Keys.CITY_KEY) ?: ""

            Log.d("UTR", "Latitude: $latitude, Longitude: $longitude")
            saveFavoriteLocation(latitude= latitude, longitude =  longitude, city)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val favoriteFactory = FavoritesViewModelFactory(
            WeatherRepositoryImpl.getInstance(
                remoteDataSource = WeatherRemoteDataSourceImpl.getInstance(),
                localDataSource = WeatherLocalDataSourceImpl(
                    AppDatabase.getDatabase(requireActivity()).weatherDao(),
                    AppDatabase.getDatabase(requireActivity()).alarmDao()
                ),
                sharedPreferences = SharedPreferencesManager(requireActivity().getSharedPreferences(Keys.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE))

            )
        )

        viewModel = ViewModelProvider(this, favoriteFactory).get(FavoritesViewModel::class.java)
        viewModel.fetchAllFavoriteWeather()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initUi()
        setUpListeners()
        observeFavorites()

    }

    private fun setUpListeners() {
        binding.addFavoriteLocationFAB.setOnClickListener() {
            if(InternetChecker(requireActivity()).isInternetAvailable()){
                navigateToMaps()

            }else{
                Toast.makeText(context, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initUi() {
        binding.favoritesRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)


        favoritesAdapter = FavoritesAdapter(mutableListOf(), this)
        binding.favoritesRecyclerView.adapter = favoritesAdapter


    }

    private fun observeFavorites() {
        lifecycleScope.launch {
            viewModel.favorites.collect { state ->
                when (state) {
                    is DataState.Loading -> {
                    }
                    is DataState.Success -> {
                        favoritesAdapter.updateList(state.data)
                        observeNoFavorites(state.data.isNotEmpty())
                    }
                    is DataState.Error -> {
                        Toast.makeText(context, getString(state.message), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun observeNoFavorites(favoriteExist: Boolean) {
        if (favoriteExist) {
            Log.d("Favorites", "Favorites exist")
            binding.noFavoritesText.visibility = View.GONE
            binding.favoriteIcon.visibility = View.GONE
        } else {
            Log.d("Favorites", "Favorites not exist")
            binding.noFavoritesText.visibility = View.VISIBLE
            binding.favoriteIcon.visibility = View.VISIBLE

        }

    }

    private fun navigateToMaps() {
        mapActivityResultLauncher.launch(Intent(requireActivity(), Map::class.java))
    }

    private fun saveFavoriteLocation(latitude: Double, longitude: Double, city: String) {
        viewModel.updateWeatherAndRefreshRoom(latitude = latitude, longitude =  longitude,city =  city)
    }

    override fun onDeleteItem(weatherEntity: WeatherEntity) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(R.string.confirm_delete_title))
            .setMessage(getString(R.string.confirm_delete_message))
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                viewModel.deleteFavorite(weatherEntity)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }

        builder.create().show()
    }



    override fun onClickItem(weatherEntity: WeatherEntity) {
        val latitude = weatherEntity.latitude
        val longitude = weatherEntity.longitude
        Log.d("UTR", "MOdel Latitude: $latitude, Longitude: $longitude")
        navigateToFavoriteDetails(latitude= latitude, longitude = longitude)
    }

    private fun navigateToFavoriteDetails(latitude: Double, longitude: Double) {
        val intent = Intent(requireContext(), FavoriteDetails::class.java).apply {
            Log.d("UTR", "BEFROE navigate Latitude: $latitude, Longitude: $longitude")
            putExtra(Keys.LATITUDE_KEY, latitude)
            putExtra(Keys.LONGITUDE_KEY, longitude)
        }
        startActivity(intent)
    }

}