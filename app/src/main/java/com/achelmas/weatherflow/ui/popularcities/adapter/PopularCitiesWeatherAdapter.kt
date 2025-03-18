package com.achelmas.weatherflow.ui.popularcities.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Shader
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.achelmas.weatherflow.R
import com.achelmas.weatherflow.data.model.WeatherForecastResponse
import com.bumptech.glide.Glide

class PopularCitiesWeatherAdapter: RecyclerView.Adapter<PopularCitiesWeatherAdapter.MyViewHolder>() {

    private var popularCitiesList: List<WeatherForecastResponse> = ArrayList()
    private var unit: String = "C"

    @SuppressLint("NotifyDataSetChanged")
    fun setPopularCities(popularCitiesList: List<WeatherForecastResponse> , tempUnit: String) {
        this.popularCitiesList = popularCitiesList
        unit = tempUnit
        notifyDataSetChanged()
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var background: RelativeLayout
        var icon: ImageView
        var temperature: TextView
        var maxtemp_c: TextView
        var mintemp_c: TextView
        var city_country: TextView
        var weatherConditionText: TextView
        init {
            background = itemView.findViewById(R.id.listItemPopularCities_relativeLayoutBackground)
            icon = itemView.findViewById(R.id.listItemPopularCities_icon)
            temperature = itemView.findViewById(R.id.listItemPopularCities_temperature)
            maxtemp_c = itemView.findViewById(R.id.listItemPopularCities_maxtemp_c)
            mintemp_c = itemView.findViewById(R.id.listItemPopularCities_mintemp_c)
            city_country = itemView.findViewById(R.id.listItemPopularCities_city_country)
            weatherConditionText = itemView.findViewById(R.id.listItemPopularCities_weatherConditionText)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_popular_cities, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = this.popularCitiesList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val weatherData = popularCitiesList[position]
        weatherData.let {
            holder.city_country.text = "${it.location.name}, ${it.location.country}"
            holder.temperature.text = if (unit == "C") "${it.current.temp_c}°" else "${it.current.temp_f}°"

            holder.maxtemp_c.text = if (unit == "C") "Max: ${it.forecast.forecastday[0].day.maxtemp_c}°"
                                    else "Max: ${it.forecast.forecastday[0].day.maxtemp_f}°"
            holder.mintemp_c.text = if (unit == "C") "Min: ${it.forecast.forecastday[0].day.mintemp_c}°"
                                    else "Min: ${it.forecast.forecastday[0].day.mintemp_f}°"

            holder.weatherConditionText.text = it.current.condition.text

            val iconUrl = "https:${it.current.condition.icon}"
            Glide.with(holder.itemView.context).load(iconUrl).into(holder.icon)
        }
    }
}