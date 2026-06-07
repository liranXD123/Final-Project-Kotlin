package com.example.finalprojectweatherapp.utils

import java.util.Locale

object LanguageUtils {
    /**
     * Returns the current system language code (e.g., "he" or "en").
     * This ensures the API always returns data matching the phone's language.
     */
    fun getSystemLanguage(): String {
        val language = Locale.getDefault().language
        // OpenWeatherMap supports "he" for Hebrew and "en" for English.
        // We default to "en" if the language is not Hebrew.
        return if (language == "iw" || language == "he") "he" else "en"
    }
}