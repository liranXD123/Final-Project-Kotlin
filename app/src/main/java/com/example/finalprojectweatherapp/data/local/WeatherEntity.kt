package com.example.finalprojectweatherapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites_table")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int, // OpenWeatherMap City ID - unique and same across all languages
    val cityName: String,
    val temperature: Double,
    val description: String,
    val iconUrl: String // icon code
)