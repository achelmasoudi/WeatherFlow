package com.achelmas.weatherflow.data.model

data class WeatherResponse(
    val location: Location ,
    val current : Current
)

data class Location(
    val name: String, // City name
    val country: String,
    val lat: Double,
    val lon: Double,
    val localtime: String
)

data class Current(
    val temp_c: Float,   // Temperature in Celsius
    val temp_f: Float,
    val condition: Condition,
    val humidity: Int,    // Humidity percentage
    val wind_kph: Float,  // Wind speed in km/h
    val vis_km: Float    // Visibility in km
)

data class Condition(
    val text: String, // Weather condition (Sunny, Rainy, etc.)
    val icon: String
)