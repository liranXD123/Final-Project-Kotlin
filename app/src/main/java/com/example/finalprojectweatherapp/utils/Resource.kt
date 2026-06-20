package com.example.finalprojectweatherapp.utils

/**
 * Wrapper for async operations — used in Add Favorite search flow.
 * Fragment switches UI based on Loading / Success / Error without try-catch in the View.
 */
sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
