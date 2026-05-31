package com.example.finalprojectweatherapp.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val _isCelsius = MutableStateFlow(prefs.getBoolean(KEY_IS_CELSIUS, true))
    val isCelsius: StateFlow<Boolean> = _isCelsius

    fun setCelsius(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_CELSIUS, enabled).apply()
        _isCelsius.value = enabled
    }

    fun isCelsius(): Boolean = _isCelsius.value

    companion object {
        private const val KEY_IS_CELSIUS = "key_is_celsius"
    }
}
