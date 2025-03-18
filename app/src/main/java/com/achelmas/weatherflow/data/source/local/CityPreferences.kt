package com.achelmas.weatherflow.data.source.local

import android.content.Context
import androidx.core.content.edit

object CityPreferences {
    private const val PREFS_NAME = "WeatherFlowPrefs"
    private const val KEY_SAVED_CITY = "saved_city"

    fun saveCity(context: Context, city: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_SAVED_CITY, city)
        }
    }

    fun getSavedCity(context: Context, defaultCity: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SAVED_CITY, defaultCity) ?: defaultCity
    }
}