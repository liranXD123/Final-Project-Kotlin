package com.example.finalprojectweatherapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a single forecast entry in the local SQLite cache.
 * Stored as a flat data class, optimized for databases, unlike the nested JSON structure from the API.
 */
@Entity(tableName = "forecast_cache")
data class ForecastEntity(
    @PrimaryKey
    val id: String,
    val locationKey: String,
    val dateTime: String,
    val temperature: Double,
    val iconCode: String,
    val description: String
)