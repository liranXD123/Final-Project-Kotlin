package com.example.finalprojectweatherapp.ui.forecast

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.finalprojectweatherapp.data.remote.models.ForecastItem
import com.example.finalprojectweatherapp.databinding.ItemForecastBinding

class ForecastAdapter : ListAdapter<ForecastItem, ForecastAdapter.ForecastViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ItemForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ForecastViewHolder(private val binding: ItemForecastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ForecastItem) {
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
            binding.tvForecastTemp.text = "${item.main.temperature.toInt()}°C"
            val iconCode = item.weatherConditions.firstOrNull()?.iconCode
            Glide.with(itemView)
                .load("https://openweathermap.org/img/wn/${iconCode}@2x.png")
                .into(binding.ivForecastIcon)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ForecastItem>() {
        override fun areItemsTheSame(oldItem: ForecastItem, newItem: ForecastItem) = oldItem.dateTime == newItem.dateTime
        override fun areContentsTheSame(oldItem: ForecastItem, newItem: ForecastItem) = oldItem == newItem
    }
}