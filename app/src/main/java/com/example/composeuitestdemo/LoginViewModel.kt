package com.example.composeuitestdemo

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christelle.mrppda.helper.DataStoreHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val loginRepository: LoginRepository = LoginRepository()
) : BaseViewModel(defaultDispatcher) {
    //private val loginRepository = LoginRepository(defaultRepositoryDispatcher)
    // 初始化为LoginUiState.Init状态
    private val _loginUiEvent = MutableStateFlow<LoginUiEvent>(LoginUiEvent.Init)
    private var _loginUiState by mutableStateOf(LoginUiState())

    val loginUiState: LoginUiState
        get() {
            return _loginUiState
        }
    val loginUiEvent: StateFlow<LoginUiEvent>
        get() {
            return _loginUiEvent.asStateFlow()
        }

    // handle loginUiEvent
    fun dispatchEvent(event: LoginUiEvent) {
        when(event) {
            is LoginUiEvent.Success -> {}
            is LoginUiEvent.Error -> {}
            else -> {}
        }
    }

    private fun credentialsIsNotEmpty(): Boolean {
        return _loginUiState.username.isNotEmpty() && _loginUiState.password.isNotEmpty()
    }

    private var job: Job? = null
    private fun login() {
        job = launchCoroutine {
            loginRepository.login(_loginUiState.username, _loginUiState.password).collect {
                var errorMsg = ""
                when(it) {
                    is NetworkState.Success<*> -> {
                        val networkReturnResult = (it as NetworkState.Success).data
                        if (networkReturnResult.success && it.data.data != null) {
                            DataStoreHelper.saveLoginState(true, it.data.data.username)
                            Log.d("TAG", "login:3 ")
                            _loginUiEvent.update {
                                LoginUiEvent.Success
                            }
                        }else {
                            _loginUiEvent.update { data ->
                                LoginUiEvent.Error(it.data.message)
                            }
                        }
                        errorMsg = if (!networkReturnResult.success) networkReturnResult.message else errorMsg
                    }
                    is NetworkState.Error -> {
                        _loginUiEvent.update { data ->
                            LoginUiEvent.Error(errorMsg = if (it.e.message == null) "" else it.e.message!!)
                        }
                        errorMsg = if (it.e.message != null) it.e.message!! else errorMsg
                    }
                    is NetworkState.Loading -> {
                        _loginUiEvent.update {
                            LoginUiEvent.Loading
                        }
                    }
                    else -> {
                        return@collect
                    }
                }
                _loginUiState = _loginUiState.copy(
                    loginButtonEnable = loginUiEvent.value !is LoginUiEvent.Error,
                    isShowLoading = it is NetworkState.Loading,
                    isError = loginUiEvent.value is LoginUiEvent.Error,
                    errorMsg = errorMsg
                )
            }
        }
    }

    // password textField submit
    fun pwdSubmitCheck(error: (String) -> Unit) {
        if (credentialsIsNotEmpty()) {
            login()
        }else {
            error("username or password is empty")
        }
    }

    // login button onClick
    val loginButtonOnClick = {
        login()
    }

    // username text change listener
    val loginUsernameTextOnChangeListener: (String) -> Unit = {
        _loginUiState = _loginUiState.copy(
            username = it,
            loginButtonEnable = it.isNotEmpty() && _loginUiState.password.isNotEmpty(),
            isError = if(_loginUiState.isError) false else _loginUiState.isError)
    }

    // pwd text change listener
    val loginPasswordTextOnChangeListener: (String) -> Unit = {
        _loginUiState = _loginUiState.copy(
            password = it,
            loginButtonEnable = it.isNotEmpty() && _loginUiState.username.isNotEmpty(),
            isError = if(_loginUiState.isError) false else _loginUiState.isError)
    }

    // loading cancel
    fun cancelLoading() {
        _loginUiState = _loginUiState.copy(isShowLoading = false)
        if (loginUiEvent.value is LoginUiEvent.Loading) {
            job?.cancel()
            _loginUiEvent.update {
                LoginUiEvent.Cancel
            }
        }
    }
}

data class LoginUiState(
    val username: String = "username",
    val password: String = "password",
    val loginButtonEnable: Boolean = username.isNotEmpty() && password.isNotEmpty(),
    val isShowLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMsg: String = "",
)

sealed class LoginUiEvent {
    // Login error
    data class Error(val errorMsg: String = "error") : LoginUiEvent()
    // Login success
    data object Success : LoginUiEvent()
    // Login loading
    data object Loading : LoginUiEvent()
    // Login loading cancel
    data object Cancel : LoginUiEvent()
    // Login init
    data object Init : LoginUiEvent()
    // pwd text onChange event
    //data class PwdTextUpdate(val pwd: String): LoginUiEvent()
    // username text onChange event
    //data class UsernameTextUpdate(val username: String): LoginUiEvent()
}