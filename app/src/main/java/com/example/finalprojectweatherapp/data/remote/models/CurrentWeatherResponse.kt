package com.example.finalprojectweatherapp.data.remote.models

import com.google.gson.annotations.SerializedName

// This matches the JSON structure from OpenWeatherMap
data class CurrentWeatherResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val cityName: String,
    @SerializedName("main") val main: MainStats,
    @SerializedName("weather") val weatherConditions: List<WeatherCondition>
)

data class MainStats(
    @SerializedName("temp") val temperature: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("humidity") val humidity: Int
)

data class WeatherCondition(
    @SerializedName("main") val condition: String,       // e.g., "Clouds", "Rain"
    @SerializedName("description") val description: String,
    @SerializedName("icon") val iconCode: String         // Used to fetch the Glide image
)