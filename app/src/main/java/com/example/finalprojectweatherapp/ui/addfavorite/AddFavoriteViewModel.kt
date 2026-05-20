package com.example.finalprojectweatherapp.ui.addfavorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalprojectweatherapp.data.local.WeatherEntity
import com.example.finalprojectweatherapp.data.remote.models.CurrentWeatherResponse
import com.example.finalprojectweatherapp.data.repository.WeatherRepository
import com.example.finalprojectweatherapp.utils.Constants
import com.example.finalprojectweatherapp.utils.Resource
import com.example.finalprojectweatherapp.utils.WeatherIconLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddFavoriteViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _searchResult = MutableLiveData<Resource<CurrentWeatherResponse>>()
    val searchResult: LiveData<Resource<CurrentWeatherResponse>> = _searchResult

    fun searchCity(cityName: String) {
        _searchResult.value = Resource.Loading()
        viewModelScope.launch {
            _searchResult.value = repository.fetchWeatherByCity(cityName, Constants.API_KEY)
        }
    }

    fun saveToFavorites(data: CurrentWeatherResponse) {
        viewModelScope.launch {
            val entity = WeatherEntity(
                cityName = data.cityName,
                temperature = data.main.temperature,
                description = data.weatherConditions.firstOrNull()?.description ?: "",
                iconUrl = WeatherIconLoader.iconUrl(data.weatherConditions.firstOrNull()?.iconCode)
            )
            repository.addToFavorites(entity)
        }
    }
}