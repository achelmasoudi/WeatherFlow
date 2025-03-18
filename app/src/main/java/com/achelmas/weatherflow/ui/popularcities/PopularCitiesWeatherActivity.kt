package com.achelmas.weatherflow.ui.popularcities

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.achelmas.weatherflow.R
import com.achelmas.weatherflow.data.source.local.TemperatureUnitPreferences
import com.achelmas.weatherflow.ui.popularcities.adapter.PopularCitiesWeatherAdapter
import com.achelmas.weatherflow.viewModel.WeatherViewModel
import com.google.android.material.snackbar.Snackbar

class PopularCitiesWeatherActivity : AppCompatActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var toolbar: Toolbar

    private lateinit var rvPopularCities: RecyclerView
    private lateinit var popularCitiesWeatherAdapter: PopularCitiesWeatherAdapter

    private lateinit var progressBar: ProgressBar

    private var noInternetSnackbar: Snackbar? = null
    private var isPopularCitiesLoaded: Boolean = false

    private lateinit var unit: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popular_cities_weather)

        initializeViews()

        unit = TemperatureUnitPreferences.getTemperatureUnit(this)

        setUpBackButton()

        setupNetworkMonitoring()

        weatherViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE  else View.GONE
            rvPopularCities.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.popularCitiesWeatherActivity_toolbar)
        progressBar = findViewById(R.id.popularCitiesWeatherActivity_progressBar)
    }

    private fun setUpBackButton() {
        toolbar.setNavigationIcon(R.drawable.arrow_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupNetworkMonitoring() {
        weatherViewModel.isNetworkAvailable.observe(this) { isConnected ->
            if (!isConnected) {
                // Show "No Internet" message when connection is lost
                noInternetSnackbar?.dismiss()
                noInternetSnackbar = Snackbar.make(findViewById(android.R.id.content), "No Internet Connection", Snackbar.LENGTH_INDEFINITE)
                    .setBackgroundTint(ContextCompat.getColor(this, R.color.list_item_color))
                    .setTextColor(ContextCompat.getColor(this, R.color.white))
                noInternetSnackbar?.show()
            } else {
                // Network restored: dismiss message and fetch data silently
                noInternetSnackbar?.dismiss()
                noInternetSnackbar = null

                // Only load data if it hasn't been loaded yet
                if (!isPopularCitiesLoaded) {
                    setupPopularCitiesList()
                }
            }
        }
        weatherViewModel.checkNetwork(this)
    }

    private fun setupPopularCitiesList() {
        rvPopularCities = findViewById(R.id.popularCitiesWeatherActivity_recyclerView)
        rvPopularCities.layoutManager = LinearLayoutManager(this)
        popularCitiesWeatherAdapter = PopularCitiesWeatherAdapter()
        rvPopularCities.adapter = popularCitiesWeatherAdapter

        // List of popular cities
        val popularCities = listOf(
            "New York", "Los Angeles", "Marrakesh", "Miami", "San Francisco",
            "London", "Manchester", "Paris", "Rome", "Berlin",
            "Zurich", "Geneva", "Moscow", "Saint Petersburg", "Madrid",
            "Barcelona", "Amsterdam", "Stockholm", "Oslo", "Copenhagen",
            "Sydney", "Istanbul", "Cape Town", "Rio de Janeiro", "Buenos Aires",
            "Tokyo", "Seoul", "Bangkok", "Dubai", "Mumbai"
        )

        weatherViewModel.getPopularCitiesWeather(popularCities).observe(this) {
            it.let {
                popularCitiesWeatherAdapter.setPopularCities(it , unit)
                isPopularCitiesLoaded = true
            }
        }
    }

}