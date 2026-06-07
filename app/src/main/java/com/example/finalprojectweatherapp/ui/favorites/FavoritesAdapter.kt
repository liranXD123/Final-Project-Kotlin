package com.example.finalprojectweatherapp.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectweatherapp.data.local.WeatherEntity
import com.example.finalprojectweatherapp.utils.TemperatureUtils
import com.example.finalprojectweatherapp.utils.WeatherIconLoader
import com.example.finalprojectweatherapp.databinding.ItemFavoriteBinding

class FavoritesAdapter(
    private val onItemClicked: (WeatherEntity) -> Unit
) : ListAdapter<WeatherEntity, FavoritesAdapter.FavoriteViewHolder>(FavoritesDiffCallback()) {

    var isCelsius: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem, isCelsius)
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

        fun bind(weather: WeatherEntity, isCelsius: Boolean) {
            binding.tvSavedCityName.text = weather.cityName
            binding.tvSavedTemperature.text = TemperatureUtils.format(weather.temperature, isCelsius)
            binding.tvSavedDescription.text = weather.description.replaceFirstChar { it.uppercase() }

            WeatherIconLoader.loadFromStored(binding.ivSavedWeatherIcon, weather.iconUrl)
        }
    }

    // This makes sure the RecyclerView only animates items that actually changed
    class FavoritesDiffCallback : DiffUtil.ItemCallback<WeatherEntity>() {
        override fun areItemsTheSame(oldItem: WeatherEntity, newItem: WeatherEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WeatherEntity, newItem: WeatherEntity): Boolean {
            return oldItem == newItem
        }
    }
}