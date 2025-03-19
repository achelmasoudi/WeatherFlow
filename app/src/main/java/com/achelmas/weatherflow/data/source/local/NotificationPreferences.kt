package com.achelmas.weatherflow.data.source.local

import android.content.Context
import androidx.core.content.edit

object NotificationPreferences {
    private const val PREFS_NAME = "WeatherFlowPrefs"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"

    fun saveNotificationPreference(context: Context, isEnabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_NOTIFICATIONS_ENABLED, isEnabled)
        }
    }

    fun getNotificationPreference(context: Context, defaultValue: Boolean = true): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, defaultValue)
    }
}