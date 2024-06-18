package com.example.composeuitestdemo

data class NetworkReturnResult<out T>(
    val success: Boolean = false,
    val message: String = "",
    val data: T?,
    val errorCode: Int? = 0
)