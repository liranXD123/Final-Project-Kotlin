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

    fun getLastUsedLanguage(): String = prefs.getString(KEY_LAST_LANG, "") ?: ""
    fun setLastUsedLanguage(lang: String) = prefs.edit().putString(KEY_LAST_LANG, lang).apply()

    fun getLastUsedUnit(): Boolean = prefs.getBoolean(KEY_LAST_UNIT, true)
    fun setLastUsedUnit(isCelsius: Boolean) = prefs.edit().putBoolean(KEY_LAST_UNIT, isCelsius).apply()

    companion object {
        private const val KEY_IS_CELSIUS = "key_is_celsius"
        private const val KEY_LAST_LANG = "key_last_lang"
        private const val KEY_LAST_UNIT = "key_last_unit"
    }
}
