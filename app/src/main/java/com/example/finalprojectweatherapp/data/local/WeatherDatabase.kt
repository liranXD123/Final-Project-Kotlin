package com.example.finalprojectweatherapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room database for the app — favorites_table and forecast_cache.
 * Single DB file "weather_db" — favorites_table + forecast_cache.
 */
@Database(entities = [WeatherEntity::class, ForecastEntity::class], version = 3, exportSchema = false)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao

}
