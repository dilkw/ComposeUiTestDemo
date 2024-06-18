package com.example.composeuitestdemo

data class LoginResult<out T> (
    val error: T? = null,
    val username: String = "",
    val path: String = "",
    val timestamp: Long? = null
)