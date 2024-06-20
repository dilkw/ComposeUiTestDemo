package com.example.composeuitestdemo

interface ApiService {

    suspend fun login(username: String, password: String): NetworkReturnResult<LoginResult<*>>
}