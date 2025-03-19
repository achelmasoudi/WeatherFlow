package com.achelmas.weatherflow.ui.settings

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
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
import com.achelmas.weatherflow.viewModel.WeatherViewModel
import com.achelmas.weatherflow.worker.WeatherNotificationWorker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {

    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var toolbar: Toolbar

    private val defaultCity = "Casablanca"
    private lateinit var currentCity: String

    private var noInternetSnackbar: Snackbar? = null

    private lateinit var savedCity: TextView
    private lateinit var changeSavedCityBtn: CardView

    private lateinit var temperatureUnit: TextView
    private lateinit var temperatureUnitBtn: CardView

    private lateinit var notificationsSwitch: SwitchMaterial
    private lateinit var notificationsBtn: CardView

    private lateinit var aboutBtn: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initializeViews()

        setUpBackButton()

        setupNetworkMonitoring()

        // Display the current saved city
        currentCity = CityPreferences.getSavedCity(this, defaultCity)
        savedCity.text = currentCity

        // Show current temperature unit
        val currentUnit = TemperatureUnitPreferences.getTemperatureUnit(this)
        temperatureUnit.text = if (currentUnit == "C") "Celsius °C" else "Fahrenheit °F"

        // Set up the notification switch
        val isNotificationsEnabled = NotificationPreferences.getNotificationPreference(this)
        notificationsSwitch.isChecked = isNotificationsEnabled

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save the new state using NotificationPreferences
            NotificationPreferences.saveNotificationPreference(this, isChecked)

            // Schedule or cancel the notification
            if (isChecked) {
                scheduleWeatherNotification()
            } else {
                cancelWeatherNotification()
            }
        }

        changeSavedCityBtn.setOnClickListener { showChangeCityDialog() }

        temperatureUnitBtn.setOnClickListener { showTemperatureUnitDialog() }

        aboutBtn.setOnClickListener { showAboutDialog() }
    }

    private fun initializeViews() {
        toolbar = findViewById(R.id.settingsActivity_toolbar)

        savedCity = findViewById(R.id.settingsActivity_savedCity)
        changeSavedCityBtn = findViewById(R.id.settingsActivity_changeSavedCityBtn)

        temperatureUnit = findViewById(R.id.settingsActivity_temperatureUnit)
        temperatureUnitBtn = findViewById(R.id.settingsActivity_temperatureUnitBtn)

        notificationsSwitch = findViewById(R.id.settingsActivity_notificationsSwitch)
        notificationsBtn = findViewById(R.id.settingsActivity_notificationsBtn)

        aboutBtn = findViewById(R.id.settingsActivity_aboutBtn)
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
            }
        }
        weatherViewModel.checkNetwork(this)
    }

    private fun showTemperatureUnitDialog() {
        val bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_temperature_unit,
            findViewById(R.id.bottomSheet_temperatureUnit_containerId))

        val bottomSheetDialog = BottomSheetDialog(this, R.style.bottomSheetDialogTheme)

        // Set up RadioButtons
        val radioCelsius: RadioButton = bottomSheetView.findViewById(R.id.radioBtn_celsius)
        val radioFahrenheit: RadioButton = bottomSheetView.findViewById(R.id.radioBtn_fahrenheit)
        val linearCelsius: LinearLayout = bottomSheetView.findViewById(R.id.linearLayout_radioBtn_celsius)
        val linearFahrenheit: LinearLayout = bottomSheetView.findViewById(R.id.linearLayout_radioBtn_fahrenheit)

        // Set initial state
        val currentUnit = TemperatureUnitPreferences.getTemperatureUnit(this)
        radioCelsius.isChecked = currentUnit == "C"
        radioFahrenheit.isChecked = currentUnit == "F"

        // Handle clicks on LinearLayouts
        linearCelsius.setOnClickListener {
            radioCelsius.isChecked = true
            TemperatureUnitPreferences.saveTemperatureUnit(this, "C")
            temperatureUnit.text = "Celsius °C"
            bottomSheetDialog.dismiss()
        }
        linearFahrenheit.setOnClickListener {
            radioFahrenheit.isChecked = true
            TemperatureUnitPreferences.saveTemperatureUnit(this, "F")
            temperatureUnit.text = "Fahrenheit °F"
            bottomSheetDialog.dismiss()
        }

        // Handle clicks on RadioButtons
        radioCelsius.setOnClickListener {
            TemperatureUnitPreferences.saveTemperatureUnit(this, "C")
            temperatureUnit.text = "Celsius °C"
            bottomSheetDialog.dismiss()
        }
        radioFahrenheit.setOnClickListener {
            TemperatureUnitPreferences.saveTemperatureUnit(this, "F")
            temperatureUnit.text = "Fahrenheit °F"
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()

    }

    private fun showChangeCityDialog() {
        val bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_change_city, findViewById(R.id.bottomSheet_changeCity_containerId))
        val bottomSheetDialog = BottomSheetDialog(this, R.style.bottomSheetDialogTheme)

        val searchEditText: TextInputEditText = bottomSheetView.findViewById(R.id.bottomSheet_searchCity_editText)
        val citySearchRecyclerView: RecyclerView = bottomSheetView.findViewById(R.id.bottomSheet_citySearch_recyclerView)

        // Set up RecyclerView
        citySearchRecyclerView.layoutManager = LinearLayoutManager(this)
        val citySearchAdapter = CitySearchAdapter { selectedCity ->
            val builder = MaterialAlertDialogBuilder(this)
            builder.let {
                it.setTitle("Change Saved City")
                it.setMessage("Are you sure you want to change $currentCity to $selectedCity?")
                it.setPositiveButton("Yes") { _, _ ->

                    CityPreferences.saveCity(this, selectedCity)
                    savedCity.text = selectedCity

                    bottomSheetDialog.dismiss()
                }
                it.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                it.setCancelable(false)
                val customDialog = it.create()
                customDialog.show()
            }
        }
        citySearchRecyclerView.adapter = citySearchAdapter

        // Set up search functionality
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchQuery = s.toString()
                if (searchQuery.isNotEmpty()) {
                    weatherViewModel.getCitySearchByName(searchQuery).observe(this@SettingsActivity) { cities ->
                        cities?.let { citySearchAdapter.setCitySearch(it) }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Fix keyboard overlap
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.behavior.isFitToContents = true
        bottomSheetDialog.show()

        // Request focus and show keyboard immediately
        searchEditText.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0) // Force keyboard open
    }

    private fun scheduleWeatherNotification() {
        val workRequest = PeriodicWorkRequestBuilder<WeatherNotificationWorker>(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "weather_notification_work",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }

    private fun cancelWeatherNotification() {
        WorkManager.getInstance(this)
            .cancelUniqueWork("weather_notification_work")
    }

    private fun showAboutDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_about, null)

        val titleTextView: TextView = dialogView.findViewById(R.id.about_applicationTitle)

        // Create a LinearGradient shader
        val paint = titleTextView.paint
        val width = paint.measureText(titleTextView.text.toString())
        val textShader = LinearGradient(
            0f, 0f, width, titleTextView.textSize,
            intArrayOf(
                ContextCompat.getColor(this , R.color.third_color),  // start
                ContextCompat.getColor(this , R.color.second_color)   // end
            ),
            null,  // No specific positions, let it flow evenly
            Shader.TileMode.CLAMP
        )
        // Apply the gradient to the TextView
        titleTextView.paint.shader = textShader
        titleTextView.invalidate()  // Redraw to show the gradient

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ -> }
            .setCancelable(false)
        val customDialog = dialog.create()
        customDialog.show()
    }

    override fun onResume() {
        super.onResume()
        // Update the saved city
        val currentCity = CityPreferences.getSavedCity(this, defaultCity)
        savedCity.text = currentCity

        val currentUnit = TemperatureUnitPreferences.getTemperatureUnit(this)
        temperatureUnit.text = if (currentUnit == "C") "Celsius °C" else "Fahrenheit °F"
    }
}