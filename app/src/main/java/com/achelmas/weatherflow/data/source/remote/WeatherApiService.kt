package com.achelmas.weatherflow.data.source.remote

import com.achelmas.weatherflow.data.model.City
import com.achelmas.weatherflow.data.model.WeatherForecastResponse
import com.achelmas.weatherflow.data.model.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("current.json")
    fun getCurrentWeather(@Query("key") apiKey: String , @Query("q") city: String) : Call<WeatherResponse>

    @GET("forecast.json")
    fun getWeatherForecast(@Query("key") apiKey: String , @Query("q") city: String , @Query("days") days: Int = 8) : Call<WeatherForecastResponse>

    @GET("search.json")
    fun getCitySearchByName(@Query("key") apiKey: String , @Query("q") city: String) : Call<List<City>>
}