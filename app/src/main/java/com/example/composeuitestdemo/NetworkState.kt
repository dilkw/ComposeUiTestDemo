package com.example.composeuitestdemo

sealed class NetworkState<out T> {
    data class Success<out T>(val data: T) : NetworkState<T>()
    data class Error(val e: Throwable) : NetworkState<Nothing>()
    data object Loading : NetworkState<Nothing>()
    data object INIT : NetworkState<Nothing>()
}
