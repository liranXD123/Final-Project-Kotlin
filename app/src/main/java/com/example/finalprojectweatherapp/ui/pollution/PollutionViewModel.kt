// package declaration for the pollution ui feature
package com.example.finalprojectweatherapp.ui.pollution

// import statements for android lifecycle, coroutines, hilt, and project dependencies
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalprojectweatherapp.data.remote.models.PollutionResponse
import com.example.finalprojectweatherapp.data.repository.WeatherRepository
import com.example.finalprojectweatherapp.utils.Constants
import com.example.finalprojectweatherapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// annotate with @hiltviewmodel to allow hilt to inject dependencies into this viewmodel
@HiltViewModel
// define pollutionviewmodel class extending viewmodel, with injected constructor
class PollutionViewModel @Inject constructor(
    // inject weatherrepository to handle data fetching operations
    private val repository: WeatherRepository
) : ViewModel() {

    // declare a private mutable live data to hold the current internal pollution state
    private val _pollutionState = MutableLiveData<Resource<PollutionResponse>>()
    // expose a public immutable live data for the ui to observe pollution state changes
    val pollutionState: LiveData<Resource<PollutionResponse>> = _pollutionState

    /*
     * fetches air pollution data for the specified coordinates and updates the ui state.
     * * parameters:
     * lat - the latitude coordinate for the target location
     * lon - the longitude coordinate for the target location
     * * returns: none
     */
    fun getPollutionData(lat: Double, lon: Double) {
        // set the initial state to loading before starting the network request
        _pollutionState.value = Resource.Loading()
        // launch a coroutine in the viewmodel scope to perform the asynchronous network call
        viewModelScope.launch {
            // fetch the pollution data from the repository using the api key and update the live data state
            _pollutionState.value = repository.fetchAirPollution(lat, lon, Constants.API_KEY)
        }
    }
}