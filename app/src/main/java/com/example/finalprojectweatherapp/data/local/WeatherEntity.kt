package com.example.finalprojectweatherapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites_table")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = false)
    val cityName: String, // Using city name as the unique key prevents duplicates
    val temperature: Double,
    val description: String,
    val iconUrl: String // Required to load the weather icon using Glide
)