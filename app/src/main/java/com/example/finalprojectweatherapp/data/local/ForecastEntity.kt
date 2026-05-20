package com.example.finalprojectweatherapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

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
