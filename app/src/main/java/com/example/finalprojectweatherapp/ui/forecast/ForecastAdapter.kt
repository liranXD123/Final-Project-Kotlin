package com.example.finalprojectweatherapp.ui.forecast

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectweatherapp.data.remote.models.ForecastItem
import com.example.finalprojectweatherapp.utils.TemperatureUtils
import com.example.finalprojectweatherapp.utils.WeatherIconLoader
import com.example.finalprojectweatherapp.databinding.ItemForecastBinding

class ForecastAdapter : ListAdapter<ForecastItem, ForecastAdapter.ForecastViewHolder>(DiffCallback()) {

    var isCelsius: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ItemForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(getItem(position), isCelsius)
    }

    class ForecastViewHolder(private val binding: ItemForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ForecastItem, isCelsius: Boolean) {
            // Prettier date formatting
            val displayDate = try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("EEE, MMM d • HH:mm", java.util.Locale.getDefault())
                val date = inputFormat.parse(item.dateTime)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                item.dateTime
            }

            binding.tvForecastTime.text = displayDate
            binding.tvForecastTemp.text = TemperatureUtils.format(item.main.temperature, isCelsius)
            
            WeatherIconLoader.load(
                binding.ivForecastIcon,
                item.weatherConditions.firstOrNull()?.iconCode
            )
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ForecastItem>() {
        override fun areItemsTheSame(oldItem: ForecastItem, newItem: ForecastItem) = oldItem.dateTime == newItem.dateTime
        override fun areContentsTheSame(oldItem: ForecastItem, newItem: ForecastItem) = oldItem == newItem
    }
}