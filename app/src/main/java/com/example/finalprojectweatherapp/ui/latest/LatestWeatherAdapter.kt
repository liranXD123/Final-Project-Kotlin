package com.example.finalprojectweatherapp.ui.latest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.finalprojectweatherapp.data.remote.models.CurrentWeatherResponse
import com.example.finalprojectweatherapp.databinding.ItemLatestWeatherBinding

class LatestWeatherAdapter : ListAdapter<CurrentWeatherResponse, LatestWeatherAdapter.LatestViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatestViewHolder {
        val binding = ItemLatestWeatherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LatestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LatestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LatestViewHolder(private val binding: ItemLatestWeatherBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CurrentWeatherResponse) {
            binding.tvLatestCity.text = item.cityName
            binding.tvLatestTemp.text = "${item.main.temperature.toInt()}°C"
            val icon = item.weatherConditions.firstOrNull()?.iconCode
            Glide.with(itemView).load("https://openweathermap.org/img/wn/$icon.png").into(binding.ivLatestIcon)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CurrentWeatherResponse>() {
        override fun areItemsTheSame(oldItem: CurrentWeatherResponse, newItem: CurrentWeatherResponse) = oldItem.cityName == newItem.cityName
        override fun areContentsTheSame(oldItem: CurrentWeatherResponse, newItem: CurrentWeatherResponse) = oldItem == newItem
    }
}