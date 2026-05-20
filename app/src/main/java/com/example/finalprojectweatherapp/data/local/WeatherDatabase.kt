package com.example.finalprojectweatherapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WeatherEntity::class, ForecastEntity::class], version = 2, exportSchema = false)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao

}