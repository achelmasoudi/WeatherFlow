package com.achelmas.weatherflow.ui.next7days

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.achelmas.weatherflow.R
import com.achelmas.weatherflow.data.source.local.TemperatureUnitPreferences
import com.achelmas.weatherflow.ui.main.adapter.HourlyWeatherAdapter
import com.achelmas.weatherflow.ui.next7days.adapter.Next7DaysAdapter
import com.achelmas.weatherflow.viewModel.WeatherViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar

class Next7DaysActivity : AppCompatActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var toolbar: Toolbar
    private lateinit var currentCity: String

    private lateinit var icon: ImageView
    private lateinit var weatherConditionText: TextView
    private lateinit var temperatureMaxMin: TextView
    private lateinit var wind: TextView
    private lateinit var humidity: TextView
    private lateinit var visibility: TextView

    private lateinit var rvNext7Days: RecyclerView
    private lateinit var next7DaysAdapter: Next7DaysAdapter

    private lateinit var progressBar: ProgressBar
    private lateinit var mainView: RelativeLayout

    private var noInternetSnackbar: Snackbar? = null
    private var is7DaysLoaded: Boolean = false

    private lateinit var unit: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_next7_days)

        initializeViews()

        unit = TemperatureUnitPreferences.getTemperatureUnit(this)

        getCurrentCity()

        setUpBackButtonAndToolbarTitle()

        setupNetworkMonitoring()

        weatherViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE  else View.GONE
            mainView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.next7DaysActivity_toolbar)

        icon = findViewById(R.id.next7DaysActivity_icon)
        weatherConditionText = findViewById(R.id.next7DaysActivity_weatherConditionText)
        temperatureMaxMin = findViewById(R.id.next7DaysActivity_temperature)
        wind = findViewById(R.id.next7DaysActivity_wind)
        humidity = findViewById(R.id.next7DaysActivity_humidity)
        visibility = findViewById(R.id.next7DaysActivity_visibility)

        progressBar = findViewById(R.id.next7DaysActivity_progressBar)
        mainView = findViewById(R.id.next7DaysActivity_relativeLayout_mainView)
    }

    private fun setUpBackButtonAndToolbarTitle() {
        toolbar.setNavigationIcon(R.drawable.arrow_back)
        toolbar.title = "Next 7 Days in ${currentCity}"
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun getCurrentCity() {
        // Retrieve the Bundle from the Intent
        val bundle = intent.extras
        currentCity = bundle?.getString("currentCity").toString()
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
                if (!is7DaysLoaded) {
                    loadTomorrowWeather(currentCity)
                    setupNext7DaysList()
                }
            }
        }
        weatherViewModel.checkNetwork(this)
    }

    private fun loadTomorrowWeather(cityName: String) {
        weatherViewModel.getWeatherForecast(cityName).observe(this) {
            if (it != null) {
                Glide.with(this).load("https:${it.forecast.forecastday[1].day.condition.icon}").into(icon)
                weatherConditionText.text = it.forecast.forecastday[1].day.condition.text

                val maxTemp = if (unit == "C") it.forecast.forecastday[1].day.maxtemp_c.toString()
                              else it.forecast.forecastday[1].day.maxtemp_f.toString()
                val minTemp = if (unit == "C") it.forecast.forecastday[1].day.mintemp_c.toString()
                              else it.forecast.forecastday[1].day.mintemp_f.toString()
                temperatureMaxMin.text = next7DaysAdapter.getColoredTemperatureText(this , maxTemp , minTemp )

                wind.text = "${it.forecast.forecastday[1].day.maxwind_kph} km/h"
                humidity.text = "${it.forecast.forecastday[1].day.avghumidity} %"
                visibility.text = "${it.forecast.forecastday[1].day.avgvis_km} km"
            }
        }
    }

    private fun setupNext7DaysList() {
        rvNext7Days = findViewById(R.id.next7DaysActivity_recyclerView)
        rvNext7Days.layoutManager = LinearLayoutManager(this)
        next7DaysAdapter = Next7DaysAdapter()
        rvNext7Days.adapter = next7DaysAdapter

        weatherViewModel.getWeatherForecast(currentCity).observe(this) {
            if (it != null) {
                next7DaysAdapter.setNext7Days(it.forecast.forecastday , unit)
                is7DaysLoaded = true
            }
        }
    }
}