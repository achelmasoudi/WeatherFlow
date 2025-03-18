package com.achelmas.weatherflow.data.model

data class WeatherForecastResponse (
    val location: Location,
    val current: Current,
    val forecast: Forecast
)

data class Forecast(
    val forecastday: List<Forecastday>
)

data class Forecastday(
    val date: String,
    val day: Day,
    val hour: List<Hour>
)

data class Day(
    val maxtemp_c: Float,
    val maxtemp_f: Float,
    val mintemp_c: Float,
    val mintemp_f: Float,
    val maxwind_kph: Float, // Wind ( km/h )
    val avgvis_km: Float, // Visibility ( km )
    val avghumidity: Int, // Humidity ( % )
    val condition: Condition
)

data class Hour(
    val time: String,
    val temp_c: Float,
    val temp_f: Float,
    val condition: Condition
)



