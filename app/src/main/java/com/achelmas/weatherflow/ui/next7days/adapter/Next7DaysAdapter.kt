package com.achelmas.weatherflow.ui.next7days.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.achelmas.weatherflow.R
import com.achelmas.weatherflow.data.model.Forecastday
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class Next7DaysAdapter: RecyclerView.Adapter<Next7DaysAdapter.MyViewHolder>(){

    private var next7DaysList: List<Forecastday> = ArrayList()
    private var unit: String = "C"

    @SuppressLint("NotifyDataSetChanged")
    fun setNext7Days(next7DaysList: List<Forecastday> , tempUnit: String) {
        // take the list starting from the 3rd day (index 2) if there are more than 2 days
        this.next7DaysList = if (next7DaysList.size > 2) {
            next7DaysList.drop(2)
        } else {
            emptyList()
        }

        unit = tempUnit
        notifyDataSetChanged()
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var day: TextView
        var date: TextView
        var icon: ImageView
        var weatherConditionText: TextView
        var temperature: TextView
        init {
            day = itemView.findViewById(R.id.listItemNext7Days_day)
            date = itemView.findViewById(R.id.listItemNext7Days_date)
            icon = itemView.findViewById(R.id.listItemNext7Days_icon)
            weatherConditionText = itemView.findViewById(R.id.listItemNext7Days_weatherConditionText)
            temperature = itemView.findViewById(R.id.listItemNext7Days_temperature)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_next7_days, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = next7DaysList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val day: Forecastday = next7DaysList.get(position)
        day.let {
            holder.day.text = getDayOfWeek( it.date )
            holder.date.text = getFormattedDate(it.date)
            Glide.with(holder.itemView).load("https:${it.day.condition.icon}").into(holder.icon)
            holder.weatherConditionText.text = it.day.condition.text

            val maxTemp = if (unit == "C") it.day.maxtemp_c.toString() else it.day.maxtemp_f.toString()
            val minTemp = if (unit == "C") it.day.mintemp_c.toString() else it.day.mintemp_f.toString()
            holder.temperature.text = getColoredTemperatureText(holder.itemView.context , maxTemp , minTemp)
        }
    }

    private fun getDayOfWeek(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateString) ?: return "Unknown"
            val outputFormat = SimpleDateFormat("EEEE", Locale.getDefault()) // "EEEE" gives full day name
            outputFormat.format(date)
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getFormattedDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateString) ?: return "Unknown"
            val outputFormat = SimpleDateFormat("MMM d", Locale.getDefault()) // "MMM d" gives "Mar 27"
            outputFormat.format(date)
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getColoredTemperatureText(context: Context, maxTemp: String, minTemp: String): SpannableString {
        val separator = " / "
        val fullText = "$maxTemp°$separator$minTemp°"

        val spannable = SpannableString(fullText)

        // Get colors
        val redColor = ContextCompat.getColor(context, R.color.red)
        val blueColor = ContextCompat.getColor(context, R.color.blue)
        val grayColor = ContextCompat.getColor(context, R.color.gray)

        // Apply red to max temperature
        spannable.setSpan(ForegroundColorSpan(redColor), 0, maxTemp.length + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Apply white to "/"
        val separatorStart = maxTemp.length + 1
        spannable.setSpan(ForegroundColorSpan(grayColor), separatorStart, separatorStart + separator.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Apply blue to min temperature
        val minTempStart = separatorStart + separator.length
        spannable.setSpan(ForegroundColorSpan(blueColor), minTempStart, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spannable
    }

}