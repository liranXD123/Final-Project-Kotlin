package com.example.finalprojectweatherapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for one saved favorite city.
 *
 * - id = OpenWeather city ID (stable across languages; used for refresh and duplicate prevention).
 * - iconUrl stores the icon code (e.g. "01d"), not the full URL — WeatherIconLoader builds the URL.
 */
@Entity(tableName = "favorites_table")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val cityName: String,
    val temperature: Double,
    val description: String,
    val iconUrl: String
)
