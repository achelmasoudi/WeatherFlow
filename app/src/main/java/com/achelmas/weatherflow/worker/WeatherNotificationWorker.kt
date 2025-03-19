package com.achelmas.weatherflow.worker

import android.Manifest
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.achelmas.weatherflow.R
import com.achelmas.weatherflow.data.model.WeatherForecastResponse
import com.achelmas.weatherflow.data.source.local.CityPreferences
import com.achelmas.weatherflow.data.source.local.NotificationPreferences
import com.achelmas.weatherflow.data.source.local.TemperatureUnitPreferences
import com.achelmas.weatherflow.data.source.remote.ApiClient
import com.achelmas.weatherflow.ui.main.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherNotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        // Check if notifications are enabled using NotificationPreferences
        val isNotificationsEnabled = NotificationPreferences.getNotificationPreference(applicationContext)

        if (!isNotificationsEnabled) {
            return Result.success() // Do nothing if notifications are disabled
        }

        // Get the saved city and temperature unit
        val savedCity = CityPreferences.getSavedCity(applicationContext, "Casablanca")
        val tempUnit = TemperatureUnitPreferences.getTemperatureUnit(applicationContext)

        // Fetch tomorrow's weather data
        ApiClient.instance.getWeatherForecast(ApiClient.getApiKey(), savedCity)
            .enqueue(object : Callback<WeatherForecastResponse> {
                override fun onResponse(call: Call<WeatherForecastResponse>, response: Response<WeatherForecastResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val forecast = response.body()!!
                        val tomorrow = forecast.forecast.forecastday[1]
                        val condition = tomorrow.day.condition.text
                        val maxTemp = if (tempUnit == "C") tomorrow.day.maxtemp_c else tomorrow.day.maxtemp_f
                        val minTemp = if (tempUnit == "C") tomorrow.day.mintemp_c else tomorrow.day.mintemp_f

                        sendWeatherNotification(applicationContext, savedCity, condition, maxTemp, minTemp)
                    }
                }

                override fun onFailure(call: Call<WeatherForecastResponse>, t: Throwable) {
                    // Handle failure (e.g., no internet)
                }
            })

        return Result.success()
    }

    private fun sendWeatherNotification(context: Context, city: String, condition: String, maxTemp: Float, minTemp: Float) {
        val channelId = "weather_channel"
        val notificationId = 1

        // Intent to open the app when the user taps "See full forecast"
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val title = "Tomorrow in $city: $condition"
        val body = "${maxTemp.toInt()}° / ${minTemp.toInt()}° • See full forecast"
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.small_icon_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Check for POST_NOTIFICATIONS permission before sending the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, send the notification
                with(NotificationManagerCompat.from(context)) {
                    notify(notificationId, builder.build())
                }
            } else {
                // Permission not granted, skip sending the notification
                // We can log this or schedule a retry
            }
        } else {
            // For API < 33, no permission check needed
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        }
    }
}