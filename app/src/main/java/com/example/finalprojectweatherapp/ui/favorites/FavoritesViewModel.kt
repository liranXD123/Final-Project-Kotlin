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

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _favorites = MutableLiveData<List<WeatherEntity>>()
    val favorites: LiveData<List<WeatherEntity>> = _favorites

    val isCelsius = settingsManager.isCelsius.asLiveData()

    private var allFavorites = listOf<WeatherEntity>()
    private var currentSortType = SortType.ALPHABETICAL
    private var currentQuery = ""

    enum class SortType {
        ALPHABETICAL, TEMPERATURE
    }

    init {
        observeDatabase()
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.getFavoritesFlow().collectLatest { databaseList ->
                allFavorites = databaseList
                updateList()
                
                // Detection logic: Compare current system state with PERSISTED state
                val currentLang = LanguageUtils.getSystemLanguage()
                val currentCelsius = settingsManager.isCelsius()
                
                val lastLang = settingsManager.getLastUsedLanguage()
                val lastUnit = settingsManager.getLastUsedUnit()
                
                // If it's the first run or settings changed, we trigger a refresh
                if (databaseList.isNotEmpty() && (currentLang != lastLang || currentCelsius != lastUnit)) {
                    // Update persisted state immediately to avoid repeated calls
                    settingsManager.setLastUsedLanguage(currentLang)
                    settingsManager.setLastUsedUnit(currentCelsius)

                    refreshFavoritesNetwork(databaseList)
                }
            }
        }
    }

    private fun refreshFavoritesNetwork(list: List<WeatherEntity>) {
        viewModelScope.launch {
            list.forEach { entity ->
                // Use city ID for reliable fetching across different languages
                val result = repository.fetchWeatherById(entity.id, Constants.API_KEY)
                if (result is Resource.Success && result.data != null) {
                    val weather = result.data
                    val updated = WeatherEntity(
                        id = weather.id,
                        cityName = weather.cityName, // This will now be the translated name
                        temperature = weather.main.temperature,
                        description = weather.weatherConditions.firstOrNull()?.description ?: "",
                        iconUrl = weather.weatherConditions.firstOrNull()?.iconCode ?: ""
                    )
                    repository.addToFavorites(updated)
                }
            }
        }
    }

    fun setSortType(sortType: SortType) {
        currentSortType = sortType
        updateList()
    }

    fun filter(query: String) {
        currentQuery = query
        updateList()
    }

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

    fun deleteFavorite(weather: WeatherEntity) {
        viewModelScope.launch {
            repository.removeFromFavorites(weather)
        }
    }

    fun saveFavorite(weather: WeatherEntity) {
        viewModelScope.launch {
            repository.addToFavorites(weather)
        }
    }
}