package com.achelmas.weatherflow.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.achelmas.weatherflow.data.model.City
import com.achelmas.weatherflow.data.model.WeatherForecastResponse
import com.achelmas.weatherflow.data.model.WeatherResponse
import com.achelmas.weatherflow.data.source.remote.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherRepository {

    val isLoading = MutableLiveData<Boolean>()  // Loading state

    private var currentWeatherMutableLiveData = MutableLiveData<WeatherResponse>()
    private var weatherForecastMutableLiveData = MutableLiveData<WeatherForecastResponse>()
    private var popularCitiesWeatherMutableLiveData = MutableLiveData<List<WeatherForecastResponse>>()

    private var citiesMutableLiveData = MutableLiveData<List<City>>()

    fun getCurrentWeather(city: String) : LiveData<WeatherResponse> {
        ApiClient.instance.getCurrentWeather(ApiClient.getApiKey() , city)
            .enqueue(object: Callback<WeatherResponse> {
                override fun onResponse(p0: Call<WeatherResponse>, p1: Response<WeatherResponse>) {
                    if (p1.isSuccessful) {
                        currentWeatherMutableLiveData.value = p1.body()
                    }
                }

                override fun onFailure(p0: Call<WeatherResponse>, p1: Throwable) {
                }
            })

        return currentWeatherMutableLiveData
    }

    fun getWeatherForecast(city: String) : LiveData<WeatherForecastResponse> {
        isLoading.postValue(true)  // Start loading

        ApiClient.instance.getWeatherForecast(ApiClient.getApiKey() , city)
            .enqueue(object : Callback<WeatherForecastResponse> {
                override fun onResponse(p0: Call<WeatherForecastResponse>, p1: Response<WeatherForecastResponse>) {
                    if (p1.isSuccessful) {
                        weatherForecastMutableLiveData.value = p1.body()
                    }
                    isLoading.postValue(false)  // Stop loading
                }

                override fun onFailure(p0: Call<WeatherForecastResponse>, p1: Throwable) {
                    isLoading.postValue(false)  // Stop loading on failure
                }
            })
        return weatherForecastMutableLiveData
    }

    fun getPopularCitiesWeather(cities: List<String>): LiveData<List<WeatherForecastResponse>> {
        isLoading.postValue(true)  // Start loading

        val weatherList = mutableListOf<WeatherForecastResponse>() // Temporary list
        for (city in cities) {
            ApiClient.instance.getWeatherForecast(ApiClient.getApiKey(), city)
                .enqueue(object : Callback<WeatherForecastResponse> {
                    override fun onResponse(call: Call<WeatherForecastResponse>, response: Response<WeatherForecastResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            synchronized(weatherList) { // Ensure thread safety
                                weatherList.add(response.body()!!)
                                // Update LiveData when all cities' data is collected
                                if (weatherList.size == cities.size) {
                                    popularCitiesWeatherMutableLiveData.postValue(ArrayList(weatherList))
                                }
                            }
                        }
                        isLoading.postValue(false)  // Stop loading
                    }
                    override fun onFailure(call: Call<WeatherForecastResponse>, t: Throwable) {
                        isLoading.postValue(false)  // Stop loading
                    }
                })
        }
        return popularCitiesWeatherMutableLiveData
    }

    fun getCitySearchByName(cityName: String): LiveData<List<City>> {
        isLoading.postValue(true)  // Start loading

        ApiClient.instance.getCitySearchByName(ApiClient.getApiKey() , cityName)
            .enqueue(object: Callback<List<City>> {
                override fun onResponse(p0: Call<List<City>>, p1: Response<List<City>>) {
                    if (p1.isSuccessful) {
                        citiesMutableLiveData.value = p1.body()
                    }
                    isLoading.postValue(false)  // Stop loading
                }
                override fun onFailure(p0: Call<List<City>>, p1: Throwable) {
                    isLoading.postValue(false)  // Stop loading
                }

            })
        return citiesMutableLiveData
    }
}