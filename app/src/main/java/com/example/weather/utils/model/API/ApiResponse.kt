package com.example.weather.utils.model.API

sealed class ApiResponse<out T> {
    data class Success<out T>(val data: T) : ApiResponse<T>()
    data class Error(val message: Int) : ApiResponse<Nothing>()
    object Loading : ApiResponse<Nothing>()
}


