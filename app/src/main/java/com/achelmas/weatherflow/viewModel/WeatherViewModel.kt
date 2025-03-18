package com.achelmas.weatherflow.viewModel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.achelmas.weatherflow.data.model.City
import com.achelmas.weatherflow.data.model.WeatherForecastResponse
import com.achelmas.weatherflow.data.model.WeatherResponse
import com.achelmas.weatherflow.data.repository.WeatherRepository
import com.achelmas.weatherflow.util.NetworkUtils

class WeatherViewModel: ViewModel() {

    val weatherRepo: WeatherRepository = WeatherRepository()

    val isLoading = weatherRepo.isLoading

    fun getCurrentWeather(city: String) : LiveData<WeatherResponse> = weatherRepo.getCurrentWeather(city)

    fun getWeatherForecast(city: String) : LiveData<WeatherForecastResponse> = weatherRepo.getWeatherForecast(city)

    fun getPopularCitiesWeather(cities: List<String>) : LiveData<List<WeatherForecastResponse>> = weatherRepo.getPopularCitiesWeather(cities)

    fun getCitySearchByName(cityName: String) : LiveData<List<City>> = weatherRepo.getCitySearchByName(cityName)

    val isNetworkAvailable: LiveData<Boolean> = NetworkUtils.isNetworkAvailable
    fun checkNetwork(context: Context) {
        NetworkUtils.checkNetwork(context)
    }

}