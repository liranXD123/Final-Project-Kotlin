package com.example.finalprojectweatherapp.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _isCelsius = MutableLiveData<Boolean>(true)
    val isCelsius: LiveData<Boolean> = _isCelsius

    private val _isDarkMode = MutableLiveData<Boolean>(false)
    val isDarkMode: LiveData<Boolean> = _isDarkMode

    fun setUnit(celsius: Boolean) {
        _isCelsius.value = celsius
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }
}