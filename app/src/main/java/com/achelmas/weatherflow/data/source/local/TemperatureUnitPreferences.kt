package com.achelmas.weatherflow.data.source.local

import android.content.Context
import androidx.core.content.edit

object TemperatureUnitPreferences {
    private const val PREFS_NAME = "WeatherFlowPrefs"
    private const val KEY_TEMP_UNIT = "temp_unit"

    fun saveTemperatureUnit(context: Context, unit: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_TEMP_UNIT, unit) }
    }

    fun getTemperatureUnit(context: Context, defaultUnit: String = "C"): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TEMP_UNIT, defaultUnit) ?: defaultUnit
    }
}