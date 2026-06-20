package com.example.finalprojectweatherapp.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide user preferences.
 *
 * Uses SharedPreferences for persistence + StateFlow so any screen can observe changes.
 * FavoritesViewModel also tracks last language/unit to know when to refresh city names from API.
 */
@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val _isCelsius = MutableStateFlow(prefs.getBoolean(KEY_IS_CELSIUS, true))
    val isCelsius: StateFlow<Boolean> = _isCelsius

    private val _updateInterval = MutableStateFlow(prefs.getInt(KEY_UPDATE_INTERVAL, 15))
    val updateInterval: StateFlow<Int> = _updateInterval

    /** Saves °C/°F and notifies all collectors (Favorites, Add Favorite, etc.). */
    fun setCelsius(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_CELSIUS, enabled).apply()
        _isCelsius.value = enabled
    }

    fun isCelsius(): Boolean = _isCelsius.value

    /** Compared in FavoritesViewModel to detect system language change → API refresh. */
    fun getLastUsedLanguage(): String = prefs.getString(KEY_LAST_LANG, "") ?: ""
    fun setLastUsedLanguage(lang: String) = prefs.edit().putString(KEY_LAST_LANG, lang).apply()

    fun getLastUsedUnit(): Boolean = prefs.getBoolean(KEY_LAST_UNIT, true)
    fun setLastUsedUnit(isCelsius: Boolean) = prefs.edit().putBoolean(KEY_LAST_UNIT, isCelsius).apply()

    /** Used by Latest screen / WorkManager for background refresh interval (minutes). */
    fun setUpdateInterval(minutes: Int) {
        prefs.edit().putInt(KEY_UPDATE_INTERVAL, minutes).apply()
        _updateInterval.value = minutes
    }

    fun getUpdateInterval(): Int = _updateInterval.value

    companion object {
        private const val KEY_IS_CELSIUS = "key_is_celsius"
        private const val KEY_LAST_LANG = "key_last_lang"
        private const val KEY_LAST_UNIT = "key_last_unit"
        private const val KEY_UPDATE_INTERVAL = "key_update_interval"
    }
}
