package com.example.finalprojectweatherapp.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectweatherapp.data.local.WeatherEntity
import com.example.finalprojectweatherapp.utils.WeatherIconLoader
import com.example.finalprojectweatherapp.databinding.ItemFavoriteBinding

class FavoritesAdapter(
    private val onItemClicked: (WeatherEntity) -> Unit
) : ListAdapter<WeatherEntity, FavoritesAdapter.FavoriteViewHolder>(FavoritesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class FavoriteViewHolder(private val binding: ItemFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position))
                }
            }
        }

        fun bind(weather: WeatherEntity) {
            binding.tvSavedCityName.text = weather.cityName
            binding.tvSavedTemperature.text = "${weather.temperature.toInt()}°C"
            binding.tvSavedDescription.text = weather.description.capitalize()

            WeatherIconLoader.loadFromStored(binding.ivSavedWeatherIcon, weather.iconUrl)
        }
    }

    // This makes sure the RecyclerView only animates items that actually changed
    class FavoritesDiffCallback : DiffUtil.ItemCallback<WeatherEntity>() {
        override fun areItemsTheSame(oldItem: WeatherEntity, newItem: WeatherEntity): Boolean {
            return oldItem.cityName == newItem.cityName
        }

        override fun areContentsTheSame(oldItem: WeatherEntity, newItem: WeatherEntity): Boolean {
            return oldItem == newItem
        }
    }
}