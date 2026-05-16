package com.example.finalprojectweatherapp.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.finalprojectweatherapp.data.local.WeatherDao
import com.example.finalprojectweatherapp.data.local.WeatherDatabase
import com.example.finalprojectweatherapp.data.remote.WeatherApi
import com.example.finalprojectweatherapp.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    // --- NETWORK INJECTION ---
    @Provides
    @Singleton
    fun provideWeatherApi(): WeatherApi {
        return Retrofit.Builder()
            // We assume you have a Constants file with BASE_URL = "https://api.openweathermap.org/"
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    // --- DATABASE INJECTION ---
    @Provides
    @Singleton
    fun provideWeatherDatabase(@ApplicationContext context: Context): WeatherDatabase {
        return Room.databaseBuilder(
            context,
            WeatherDatabase::class.java,
            "weather_db"
        )
            .fallbackToDestructiveMigration() // Wipes DB if you change the schema (good for dev)
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherDao(database: WeatherDatabase): WeatherDao {
        return database.weatherDao()
    }
}