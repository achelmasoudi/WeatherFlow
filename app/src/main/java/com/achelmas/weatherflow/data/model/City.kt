package com.achelmas.weatherflow.data.model

data class City(
    val id: Long,
    val name: String,
    val country: String,
    val lat: Float,
    val lon: Float
)