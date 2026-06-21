package com.example.finalprojectweatherapp.ui.latest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectweatherapp.data.remote.models.CurrentWeatherResponse
import com.example.finalprojectweatherapp.utils.TemperatureUtils
import com.example.finalprojectweatherapp.utils.WeatherIconLoader
import com.example.finalprojectweatherapp.databinding.ItemLatestWeatherBinding

/**
 * RecyclerView adapter for the multi-city live feed.
 * Uses ListAdapter + DiffUtil for efficient UI updates and prevents full list re-renders.
 */
class LatestWeatherAdapter : ListAdapter<CurrentWeatherResponse, LatestWeatherAdapter.LatestViewHolder>(DiffCallback()) {

    var isCelsius: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatestViewHolder {
        val binding = ItemLatestWeatherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LatestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LatestViewHolder, position: Int) {
        holder.bind(getItem(position), isCelsius)
    }

    class LatestViewHolder(private val binding: ItemLatestWeatherBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CurrentWeatherResponse, isCelsius: Boolean) {
            binding.tvLatestCity.text = item.cityName
            binding.tvLatestTemp.text = TemperatureUtils.format(item.main.temperature, isCelsius)
            WeatherIconLoader.load(
                binding.ivLatestIcon,
                item.weatherConditions.firstOrNull()?.iconCode
            )
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CurrentWeatherResponse>() {
        override fun areItemsTheSame(oldItem: CurrentWeatherResponse, newItem: CurrentWeatherResponse) = oldItem.cityName == newItem.cityName
        override fun areContentsTheSame(oldItem: CurrentWeatherResponse, newItem: CurrentWeatherResponse) = oldItem == newItem
    }
}