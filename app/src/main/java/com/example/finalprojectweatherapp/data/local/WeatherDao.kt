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

    // --- FAVORITES — dynamic local list ---

    /**
     * Returns a Kotlin Flow: Room re-emits whenever favorites_table changes.
     * ViewModel collects this → UI updates automatically on add/delete/edit (REPLACE).
     */
    @Query("SELECT * FROM favorites_table ORDER BY cityName ASC")
    fun getFavorites(): Flow<List<WeatherEntity>>

    /**
     * OnConflictStrategy.REPLACE = insert or update by primary key (city ID).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(weather: WeatherEntity)

    @Delete
    suspend fun deleteFavorite(weather: WeatherEntity)

    /** Used before save to avoid duplicate favorites or to show "already saved" feedback. */
    @Query("SELECT EXISTS(SELECT 1 FROM favorites_table WHERE id = :cityId)")
    suspend fun isFavoriteById(cityId: Int): Boolean

    // --- FORECAST CACHE — API → Room → UI ---

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