package com.example.finalprojectweatherapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    // Returns a Kotlin Flow. This means the ViewModel will observe this,
    // and the UI will automatically update whenever a row is added or deleted.
    @Query("SELECT * FROM favorites_table ORDER BY cityName ASC")
    fun getFavorites(): Flow<List<WeatherEntity>>

    // REPLACE strategy handles the "edit" or "update" operation seamlessly
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(weather: WeatherEntity)

    @Delete
    suspend fun deleteFavorite(weather: WeatherEntity)

    // Helper method to check if a heart icon should be filled or empty in the UI
    @Query("SELECT EXISTS(SELECT 1 FROM favorites_table WHERE id = :cityId)")
    suspend fun isFavoriteById(cityId: Int): Boolean

    // --- FORECAST CACHE (API → Room → UI) ---

    @Query("SELECT * FROM forecast_cache WHERE locationKey = :locationKey ORDER BY dateTime ASC")
    fun observeForecast(locationKey: String): Flow<List<ForecastEntity>>

    @Query("SELECT * FROM forecast_cache WHERE locationKey = :locationKey ORDER BY dateTime ASC")
    suspend fun getForecastSnapshot(locationKey: String): List<ForecastEntity>

    @Query("DELETE FROM forecast_cache WHERE locationKey = :locationKey")
    suspend fun clearForecastForLocation(locationKey: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertForecastItems(items: List<ForecastEntity>)

    @Transaction
    suspend fun replaceForecastForLocation(locationKey: String, items: List<ForecastEntity>) {
        clearForecastForLocation(locationKey)
        insertForecastItems(items)
    }
}