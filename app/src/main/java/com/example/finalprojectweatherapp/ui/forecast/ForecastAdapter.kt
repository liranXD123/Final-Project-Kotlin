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

/**
 * RecyclerView adapter for the weekly forecast list.
 * Uses ListAdapter + DiffUtil for efficient updates without reloading the entire list;
 * loads icons asynchronously via WeatherIconLoader.
 */
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

        /**
         * Binds data to the view, formats the date locally, and converts the temperature
         * using TemperatureUtils based on user settings.
         */
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

    /**
     * DiffUtil compares old vs new lists so RecyclerView only animates actual changed rows.
     */
    class DiffCallback : DiffUtil.ItemCallback<ForecastItem>() {
        override fun areItemsTheSame(oldItem: ForecastItem, newItem: ForecastItem) = oldItem.dateTime == newItem.dateTime
        override fun areContentsTheSame(oldItem: ForecastItem, newItem: ForecastItem) = oldItem == newItem
    }
}