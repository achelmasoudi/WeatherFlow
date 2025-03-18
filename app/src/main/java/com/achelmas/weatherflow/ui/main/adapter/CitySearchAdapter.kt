package com.achelmas.weatherflow.ui.main.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.achelmas.weatherflow.R
import com.achelmas.weatherflow.data.model.City

class CitySearchAdapter(private val onItemClick: (String) -> Unit): RecyclerView.Adapter<CitySearchAdapter.MyViewHolder>() {

    private var citySearchList: List<City> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    fun setCitySearch(citySearchList: List<City>) {
        this.citySearchList = citySearchList
        notifyDataSetChanged()
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val city_country: TextView
        val showBtn: CardView
        init {
            city_country = itemView.findViewById(R.id.listItemCitySearch_city_country)
            showBtn = itemView.findViewById(R.id.listItemCitySearch_showBtn)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_city_search , parent , false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = this.citySearchList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val city: City = citySearchList.get(position)
        city.let {
            holder.city_country.text = "${it.name}, ${it.country}"
        }

        holder.showBtn.setOnClickListener {
            onItemClick(city.name)
        }
    }
}