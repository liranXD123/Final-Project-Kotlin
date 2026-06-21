package com.example.finalprojectweatherapp.ui.forecast

import androidx.lifecycle.asLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalprojectweatherapp.data.local.toForecastItem
import com.example.finalprojectweatherapp.data.remote.models.ForecastItem
import com.example.finalprojectweatherapp.data.repository.WeatherRepository
import com.example.finalprojectweatherapp.utils.Constants
import com.example.finalprojectweatherapp.utils.Resource
import com.example.finalprojectweatherapp.utils.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the forecast screen.
 * Implements an Offline-First architecture (Single Source of Truth) by observing the Room database
 * via Flow, while simultaneously fetching fresh data from the API in the background.
 */
@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _forecastState = MutableLiveData<Resource<List<ForecastItem>>>()
    val forecastState: LiveData<Resource<List<ForecastItem>>> = _forecastState

    val isCelsius = settingsManager.isCelsius.asLiveData()

    private var observeJob: Job? = null

    /**
     * Observes forecast rows from Room and refreshes from the API.
     * UI updates automatically when the database changes.
     */
    fun loadForecast(lat: Double, lon: Double) {
        observeJob?.cancel()

        // Part 1: Open a continuous Flow pipe from Room DB.
        // Emits automatically when the DB table is updated.
        observeJob = viewModelScope.launch {
            repository.observeForecast(lat, lon).collectLatest { entities ->
                if (entities.isNotEmpty()) {
                    _forecastState.value = Resource.Success(entities.map { it.toForecastItem() })
                }
            }
        }

        // Part 2: Fetch fresh data from the remote API asynchronously.
        viewModelScope.launch {
            val hasCache = repository.getCachedForecast(lat, lon).isNotEmpty()
            if (!hasCache) {
                _forecastState.value = Resource.Loading()
            }

            when (val refreshResult = repository.refreshForecast(lat, lon, Constants.API_KEY)) {
                is Resource.Error -> {
                    // Fault tolerance: Only show an error if we have no cached data to display
                    if (_forecastState.value !is Resource.Success) {
                        _forecastState.value = Resource.Error(
                            refreshResult.message ?: "Network error. Check connection."
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}