package com.achelmas.weatherflow.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.achelmas.weatherflow.R
import com.achelmas.weatherflow.data.source.local.CityPreferences
import com.achelmas.weatherflow.data.source.local.NotificationPreferences
import com.achelmas.weatherflow.data.source.local.TemperatureUnitPreferences
import com.achelmas.weatherflow.ui.main.adapter.CitySearchAdapter
import com.achelmas.weatherflow.ui.main.adapter.HourlyWeatherAdapter
import com.achelmas.weatherflow.ui.next7days.Next7DaysActivity
import com.achelmas.weatherflow.ui.popularcities.PopularCitiesWeatherActivity
import com.achelmas.weatherflow.ui.popularcities.adapter.PopularCitiesWeatherAdapter
import com.achelmas.weatherflow.ui.settings.SettingsActivity
import com.achelmas.weatherflow.util.NetworkUtils
import com.achelmas.weatherflow.viewModel.WeatherViewModel
import com.achelmas.weatherflow.worker.WeatherNotificationWorker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.search.SearchView
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var searchView: SearchView
    private lateinit var toolbar: Toolbar

    private lateinit var mainView: RelativeLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var city: TextView
    private lateinit var date: TextView
    private lateinit var temperature: TextView
    private lateinit var weatherCondition: TextView
    private lateinit var maxtemp_c: TextView
    private lateinit var mintemp_c: TextView
    private lateinit var wind: TextView
    private lateinit var humidity: TextView
    private lateinit var visibility: TextView

    private val defaultCity = "Casablanca" // Default city if location permission is denied
    private var currentCity: String = defaultCity // Variable to store detected city
    private var currentUnit: String = "C" // Track current unit

    private lateinit var next7DaysBtn: CardView
    private lateinit var rvHourlyWeather: RecyclerView
    private lateinit var hourlyWeatherAdapter: HourlyWeatherAdapter

    private lateinit var rvPopularCities: RecyclerView
    private lateinit var popularCitiesWeatherAdapter: PopularCitiesWeatherAdapter

    private lateinit var viewAllCitiesBtn: CardView

    private lateinit var rvCitySearch: RecyclerView
    private lateinit var citySearchAdapter: CitySearchAdapter

    // Variable to track the Snackbar
    private var noInternetSnackbar: Snackbar? = null

    // Flags to track if data has been successfully loaded
    private var isPopularCitiesLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.statusBarColor = ContextCompat.getColor(this , R.color.third_color)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initializeViews()

        // Create notification channel
        createNotificationChannel()

        // Load saved city first
        currentCity = CityPreferences.getSavedCity(this, defaultCity)

        currentUnit = TemperatureUnitPreferences.getTemperatureUnit(this)

        // Request notification permission for Android 13+
        requestNotificationPermission()

        // Observe network availability
        setupNetworkMonitoring()

        weatherViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE  else View.GONE
            mainView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        requestLocationPermission() // Request location permission when the app starts
        setupSearchView()

        viewAllCitiesBtn.setOnClickListener {
            val intent = Intent(this@MainActivity , PopularCitiesWeatherActivity::class.java)
            startActivity(intent)
        }

        next7DaysBtn.setOnClickListener {
            val intent = Intent(this@MainActivity , Next7DaysActivity::class.java)
            intent.putExtra("currentCity" , currentCity)
            startActivity(intent)
        }

    }

    private fun initializeViews() {
        city = findViewById(R.id.mainActivity_city)
        date = findViewById(R.id.mainActivity_date)
        temperature = findViewById(R.id.mainActivity_temperature)
        weatherCondition = findViewById(R.id.mainActivity_weatherConditionText)
        maxtemp_c = findViewById(R.id.mainActivity_maxtemp_c)
        mintemp_c = findViewById(R.id.mainActivity_mintemp_c)
        wind = findViewById(R.id.mainActivity_wind)
        humidity = findViewById(R.id.mainActivity_humidity)
        visibility = findViewById(R.id.mainActivity_visibility)

        viewAllCitiesBtn = findViewById(R.id.mainActivity_viewAllCities)
        next7DaysBtn = findViewById(R.id.mainActivity_next7Days)

        searchView = findViewById(R.id.mainActivity_searchView)
        toolbar = findViewById(R.id.mainActivity_toolbar)

        mainView = findViewById(R.id.mainActivity_relativeLayout_mainView)
        progressBar = findViewById(R.id.mainActivity_progressBar)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "weather_channel"
            val channelName = "Weather Updates"
            val channelDescription = "Daily weather notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
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
                    loadWeather(currentCity)
                    setupPopularCitiesList()
                }
            }
        }
        weatherViewModel.checkNetwork(this)
    }


    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, schedule the notification worker if enabled
            val isNotificationsEnabled = NotificationPreferences.getNotificationPreference(this)
            if (isNotificationsEnabled) {
                val workRequest = PeriodicWorkRequestBuilder<WeatherNotificationWorker>(24, TimeUnit.HOURS)
                    .build()
                WorkManager.getInstance(this)
                    .enqueueUniquePeriodicWork(
                        "weather_notification_work",
                        androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                        workRequest
                    )
            }
        } else {
            // Permission denied, show a message to the user
            Snackbar.make(
                findViewById(android.R.id.content),
                "Notification permission is required to send daily weather updates.", Snackbar.LENGTH_LONG
            ).setAction("Settings") {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", packageName, null)
                    startActivity(intent)
                }
                .setBackgroundTint(ContextCompat.getColor(this, R.color.list_item_color))
                .setTextColor(ContextCompat.getColor(this, R.color.white))
                .setActionTextColor(ContextCompat.getColor(this, R.color.white))
                .show()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Permission already granted, schedule the notification worker if enabled
                val isNotificationsEnabled = NotificationPreferences.getNotificationPreference(this)
                if (isNotificationsEnabled) {
                    val workRequest = PeriodicWorkRequestBuilder<WeatherNotificationWorker>(24, TimeUnit.HOURS)
                        .build()
                    WorkManager.getInstance(this)
                        .enqueueUniquePeriodicWork(
                            "weather_notification_work",
                            ExistingPeriodicWorkPolicy.KEEP,
                            workRequest
                        )
                }
            }
        } else {
            // For API < 33, no permission needed, schedule the notification worker if enabled
            val isNotificationsEnabled = NotificationPreferences.getNotificationPreference(this)
            if (isNotificationsEnabled) {
                val workRequest = PeriodicWorkRequestBuilder<WeatherNotificationWorker>(24, TimeUnit.HOURS)
                    .build()
                WorkManager.getInstance(this)
                    .enqueueUniquePeriodicWork(
                        "weather_notification_work",
                        ExistingPeriodicWorkPolicy.KEEP,
                        workRequest
                    )
            }
        }
    }

    private fun setupPopularCitiesList() {
        rvPopularCities = findViewById(R.id.mainActivity_recyclerView_popularCities)
        rvPopularCities.layoutManager = LinearLayoutManager(this)
        popularCitiesWeatherAdapter = PopularCitiesWeatherAdapter()
        rvPopularCities.adapter = popularCitiesWeatherAdapter

        // List of popular cities
        val popularCities = listOf("New York", "Bern", "Madrid", "Tokyo", "Paris")

        weatherViewModel.getPopularCitiesWeather(popularCities).observe(this) {
            it.let {
                popularCitiesWeatherAdapter.setPopularCities(it , currentUnit )
                isPopularCitiesLoaded = true // Mark as loaded once successful
            }
        }
    }

    private val locationPermissionRequest = registerForActivityResult( ActivityResultContracts.RequestMultiplePermissions() ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            getCurrentLocation()
        } else {
            loadWeather(currentCity) // Use saved city if permission denied
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else {
            getCurrentLocation() // Permission already granted
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Please enable location services",
                    Snackbar.LENGTH_LONG
                )
                    .setAction("Settings") {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    .setBackgroundTint(ContextCompat.getColor(this, R.color.list_item_color))
                    .setTextColor(ContextCompat.getColor(this, R.color.white))
                    .setActionTextColor(ContextCompat.getColor(this, R.color.white))
                    .show()
                loadWeather(currentCity)
                return
            }

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        getCityFromLocation(location.latitude, location.longitude)
                    } else {
                        requestFreshLocation()
                    }
                }
                .addOnFailureListener {
                    requestFreshLocation()
                }
        } else {
            requestLocationPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestFreshLocation() {
        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    getCityFromLocation(location.latitude, location.longitude)
                } else {
                    loadWeather(currentCity)
                }
            }
            .addOnFailureListener {
                loadWeather(currentCity)
            }
    }

    private fun getCityFromLocation(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addressList = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addressList.isNullOrEmpty()) {
                val address = addressList[0]
                val detectedCity = address.adminArea ?: address.subAdminArea ?: address.locality ?: defaultCity

                if (detectedCity != currentCity) { // Only update if the city has changed
                    currentCity = detectedCity
                    CityPreferences.saveCity(this, currentCity) // Save the new city
                    isPopularCitiesLoaded = false
                    loadWeather(currentCity)
                }
            } else {
                loadWeather(currentCity) // Use saved city if no address found
            }
        } catch (e: Exception) {
            loadWeather(currentCity) // Use saved city on error
        }
    }

    private fun loadWeather(cityName: String) {
        weatherViewModel.getCurrentWeather(cityName).observe(this) { weatherRes ->
            weatherRes.let {
                city.text = it.location.name
                date.text = formatLocalTime(it.location.localtime)
                temperature.text = if (currentUnit == "C") "${it.current.temp_c}°" else "${it.current.temp_f}°"
                // localTime.text = it.location.localtime
                weatherCondition.text = it.current.condition.text

                wind.text = "${it.current.wind_kph} km/h"
                humidity.text = "${it.current.humidity} %"
                visibility.text = "${it.current.vis_km} km"
            }
        }

        weatherViewModel.getWeatherForecast(cityName).observe(this) {
            if (it != null) {
                maxtemp_c.text = if (currentUnit == "C") "Max: ${it.forecast.forecastday[0].day.maxtemp_c}°"
                                 else "Max: ${it.forecast.forecastday[0].day.maxtemp_f}°"
                mintemp_c.text = if (currentUnit == "C") "Min: ${it.forecast.forecastday[0].day.mintemp_c}°"
                                 else "Min: ${it.forecast.forecastday[0].day.mintemp_f}°"
            }
        }

        setupHourlyWeatherList(cityName)
    }

    private fun formatLocalTime(localTime: String): String {
        return try {
            // Input format: "yyyy-MM-dd HH:mm"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            // Parse the input string into a Date object
            val date = inputFormat.parse(localTime)

            // "EEE, dd MMM yyyy" ( "Fri, 15 Mar 2025" )
            val outputFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
            outputFormat.format(date ?: Date())

        } catch (e: Exception) {
            // Return the original string or a fallback if parsing fails
            localTime
        }
    }

    private fun setupHourlyWeatherList(cityName: String) {
        rvHourlyWeather = findViewById(R.id.mainActivity_recyclerView_hourlyWeather)
        rvHourlyWeather.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        hourlyWeatherAdapter = HourlyWeatherAdapter()
        rvHourlyWeather.adapter = hourlyWeatherAdapter

        weatherViewModel.getWeatherForecast(cityName).observe(this) {
            if (it != null) {
                hourlyWeatherAdapter.setHourlyWeather(it.forecast.forecastday[0].hour , currentUnit)

                // Scroll to "Now" position
                rvHourlyWeather.post { hourlyWeatherAdapter.scrollToNow(rvHourlyWeather) }
            }
        }
    }


    private fun setupSearchView() {
        setSupportActionBar(toolbar)
        // Configure SearchView
        searchView.apply {
            // EditText to listen for text changes
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val searchCity = s.toString()
                    setupCitySearchList(searchCity)
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            // When user presses enter
            editText.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                    val query = editText.text.toString()
                    setupCitySearchList(query)
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun setupCitySearchList(searchCity: String) {
        rvCitySearch = findViewById(R.id.mainActivity_recyclerView_citySearch)
        rvCitySearch.layoutManager = LinearLayoutManager(this)
        citySearchAdapter = CitySearchAdapter { selectedCity ->
            currentCity = selectedCity
            CityPreferences.saveCity(this, currentCity)
            loadWeather(currentCity)
            searchView.hide()
            searchView.visibility = View.GONE
        }
        rvCitySearch.adapter = citySearchAdapter

        weatherViewModel.getCitySearchByName(searchCity).observe(this) {
            if (it != null) {
                citySearchAdapter.setCitySearch(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.searchViewId -> {
                // Show SearchView when search icon is clicked
                searchView.visibility = View.VISIBLE
                searchView.show()
                true
            }
            R.id.settingsId -> {
                startActivity(Intent(this@MainActivity , SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (searchView.isShown) {
            searchView.hide()
            searchView.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkUtils.unregisterNetworkCallback() // Prevent memory leaks
    }

    override fun onResume() {
        super.onResume()

        val updatedCity = CityPreferences.getSavedCity(this, defaultCity)
        val updatedUnit = TemperatureUnitPreferences.getTemperatureUnit(this)

        // Check if city or unit changed
        if (updatedCity != currentCity || updatedUnit != currentUnit) {
            currentCity = updatedCity
            currentUnit = updatedUnit
            loadWeather(currentCity) // Refresh UI with new unit
            setupPopularCitiesList()
        }
    }
}