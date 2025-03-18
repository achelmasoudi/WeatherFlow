package com.achelmas.weatherflow.ui.main.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.achelmas.weatherflow.R
import com.achelmas.weatherflow.data.model.Hour
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class HourlyWeatherAdapter : RecyclerView.Adapter<HourlyWeatherAdapter.MyViewHolder>() {

    private var hourlyWeatherList: List<Hour> = ArrayList()
    private var unit: String = "C"

    @SuppressLint("NotifyDataSetChanged")
    fun setHourlyWeather(hourlyWeatherList: List<Hour> , tempUnit: String) {
        this.hourlyWeatherList = hourlyWeatherList
        unit = tempUnit
        notifyDataSetChanged()
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var time: TextView
        var icon: ImageView
        var temperature: TextView
        var showBtn: CardView
        init {
            time = itemView.findViewById(R.id.listItemHourlyWeather_time)
            icon = itemView.findViewById(R.id.listItemHourlyWeather_icon)
            temperature = itemView.findViewById(R.id.listItemHourlyWeather_temperature)
            showBtn = itemView.findViewById(R.id.listItemHourlyWeather_showBtn)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_hourly_weather , parent , false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = hourlyWeatherList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val hour: Hour = hourlyWeatherList.get(position)
        hour.let {
            // Convert the time
            val formattedTime = formatTimeTo12Hour(it.time)
            holder.time.text = formattedTime

            Glide.with(holder.itemView).load("https:${it.condition.icon}").into(holder.icon)
            holder.temperature.text = if (unit == "C") "${hour.temp_c}°" else "${hour.temp_f}°"

            // Get current hour in the same format (12-hour AM/PM)
            val currentHour = getCurrentHour()

            // Change background color if the hour matches the current time
            if (formattedTime == currentHour) {
                holder.showBtn.setCardBackgroundColor( holder.itemView.context.getColor(R.color.third_color) )
                holder.time.text = "Now"
            }
            else {
                holder.showBtn.setCardBackgroundColor( holder.itemView.context.getColor(R.color.list_item_color) ) // Default color
                holder.time.text = formattedTime
            }
        }
    }

    fun formatTimeTo12Hour(time: String): String {
        // Parse the time from "yyyy-MM-dd HH:mm" format
        val originalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val targetFormat = SimpleDateFormat("hh a", Locale.getDefault()) // "hh a" for 12-hour format with AM/PM
        val date = originalFormat.parse(time)
        return targetFormat.format(date)
    }

    // Function to get the current hour in the same format
    private fun getCurrentHour(): String {
        val currentTime = System.currentTimeMillis()
        val targetFormat = SimpleDateFormat("hh a", Locale.getDefault()) // "hh a" for 12-hour format with AM/PM
        return targetFormat.format(currentTime)
    }

    fun scrollToNow(recyclerView: RecyclerView) {
        // get Current Hour Position
        val currentHour = getCurrentHour()
        val currentHourPosition = hourlyWeatherList.indexOfFirst { formatTimeTo12Hour(it.time) == currentHour }

        if (currentHourPosition != -1) {
            recyclerView.scrollToPosition(currentHourPosition)
        }
    }

}