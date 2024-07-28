package com.example.composeuitestdemo

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class LoginRepository(private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default, val apiService: ApiService = ApiServiceImpl) {
    /**
     * @title 登录接口
     * @author dilkw
     * @param credentials : 登录用户信息
     * @return 登录信息，以Flow格式返回
     */
    suspend fun login(username: String, password: String): Flow<NetworkState<NetworkReturnResult<LoginResult<*>>>> = flow {
        emit(NetworkState.Loading)
        val returnResult: NetworkReturnResult<LoginResult<*>> = withContext(defaultDispatcher) {
            return@withContext apiService.login(username, password)
        }
        emit(NetworkState.Success(returnResult))
    }.onStart {
        emit(NetworkState.INIT)
    }.catch {
        emit(NetworkState.Error(it))
    }.flowOn(defaultDispatcher)
}