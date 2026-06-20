package com.example.finalprojectweatherapp.ui.favorites



import androidx.lifecycle.asLiveData

import androidx.lifecycle.LiveData

import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.ViewModel

import androidx.lifecycle.viewModelScope

import com.example.finalprojectweatherapp.data.local.WeatherEntity

import com.example.finalprojectweatherapp.data.repository.WeatherRepository

import com.example.finalprojectweatherapp.utils.Constants

import com.example.finalprojectweatherapp.utils.LanguageUtils

import com.example.finalprojectweatherapp.utils.Resource

import com.example.finalprojectweatherapp.utils.SettingsManager

import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.flow.collectLatest

import kotlinx.coroutines.launch

import javax.inject.Inject



/**
 * ViewModel for the Favorites screen.
 *
 * - Room Flow is the source of truth; UI updates automatically on insert/delete.
 * - Filter and sort are applied in memory on top of the DB list (not SQL queries).
 * - Language/unit changes trigger a network refresh by stable OpenWeather city ID.
 */

@HiltViewModel

class FavoritesViewModel @Inject constructor(

    private val repository: WeatherRepository,

    private val settingsManager: SettingsManager

) : ViewModel() {



    // Private mutable LiveData — only the ViewModel writes; Fragment observes the public val.

    private val _favorites = MutableLiveData<List<WeatherEntity>>()

    val favorites: LiveData<List<WeatherEntity>> = _favorites



    // Shared app setting (°C/°F) exposed as LiveData for the Fragment and Adapter.

    val isCelsius = settingsManager.isCelsius.asLiveData()



    // Full list from Room before client-side filter/sort is applied.

    private var allFavorites = listOf<WeatherEntity>()

    private var currentSortType = SortType.ALPHABETICAL

    private var currentQuery = ""



    enum class SortType {

        ALPHABETICAL, TEMPERATURE

    }



    init {

        observeDatabase()

    }



    /**

     * Subscribes to Room via a Kotlin Flow — core MVVM + Room pattern for this project.

     *

     * Flow: Room DB change → collectLatest → updateList() → LiveData → RecyclerView.

     * Also watches language/unit changes and re-fetches city names from the API when needed.

     *

     * viewModelScope: coroutine is cancelled automatically when the ViewModel is destroyed.

     */

    private fun observeDatabase() {

        viewModelScope.launch {

            repository.getFavoritesFlow().collectLatest { databaseList ->

                allFavorites = databaseList

                updateList()



                // Compare current device settings with what we last saved in SharedPreferences.

                val currentLang = LanguageUtils.getSystemLanguage()

                val currentCelsius = settingsManager.isCelsius()



                val lastLang = settingsManager.getLastUsedLanguage()

                val lastUnit = settingsManager.getLastUsedUnit()



                // First launch or user changed language/units → refresh translated city names from API.

                if (databaseList.isNotEmpty() && (currentLang != lastLang || currentCelsius != lastUnit)) {

                    settingsManager.setLastUsedLanguage(currentLang)

                    settingsManager.setLastUsedUnit(currentCelsius)



                    refreshFavoritesNetwork(databaseList)

                }

            }

        }

    }



    /**

     * Re-fetches each favorite from OpenWeather by city ID (not name).

     *

     * Why ID? City names change with language (e.g. "London" vs "לונדון") but ID stays the same.

     * insertFavorite uses REPLACE, so this effectively "edits" rows with updated translations.

     */

    private fun refreshFavoritesNetwork(list: List<WeatherEntity>) {

        viewModelScope.launch {

            list.forEach { entity ->

                val result = repository.fetchWeatherById(entity.id, Constants.API_KEY)

                if (result is Resource.Success && result.data != null) {

                    val weather = result.data

                    val updated = WeatherEntity(

                        id = weather.id,

                        cityName = weather.cityName,

                        temperature = weather.main.temperature,

                        description = weather.weatherConditions.firstOrNull()?.description ?: "",

                        iconUrl = weather.weatherConditions.firstOrNull()?.iconCode ?: ""

                    )

                    repository.addToFavorites(updated)

                }

            }

        }

    }



    /** User tapped sort button — re-runs filter+sort on cached list (no new DB/API call). */

    fun setSortType(sortType: SortType) {

        currentSortType = sortType

        updateList()

    }



    /** SearchView text changed — filters by city name (case-insensitive). */

    fun filter(query: String) {

        currentQuery = query

        updateList()

    }



    /**

     * Applies search filter then sort, posts result to LiveData for the Fragment to observe.

     * Separation: allFavorites = raw DB data; _favorites = what the user actually sees.

     */

    private fun updateList() {

        val filtered = if (currentQuery.isEmpty()) {

            allFavorites

        } else {

            allFavorites.filter { it.cityName.contains(currentQuery, ignoreCase = true) }

        }

        _favorites.value = applySort(filtered)

    }



    private fun applySort(list: List<WeatherEntity>): List<WeatherEntity> {

        return when (currentSortType) {

            SortType.ALPHABETICAL -> list.sortedBy { it.cityName }

            SortType.TEMPERATURE -> list.sortedByDescending { it.temperature }

        }

    }



    /** Swipe-to-delete: suspend Room delete runs on a background thread inside viewModelScope. */

    fun deleteFavorite(weather: WeatherEntity) {

        viewModelScope.launch {

            repository.removeFromFavorites(weather)

            // Room Flow emits new list → observeDatabase → updateList → UI refreshes.

        }

    }



    /** Snackbar Undo: re-inserts the same entity; REPLACE strategy avoids duplicate key errors. */

    fun saveFavorite(weather: WeatherEntity) {

        viewModelScope.launch {

            repository.addToFavorites(weather)

        }

    }

}


