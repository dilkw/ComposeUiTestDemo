package com.example.composeuitestdemo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginRepository: LoginRepository = LoginRepository()
) : ViewModel() {
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
    fun login() {
        job = viewModelScope.launch() {
            ensureActive()
            loginRepository.login(_loginUiState.username, _loginUiState.password).collect {
                var errorMsg = ""
                when(it) {
                    is NetworkState.Success<*> -> {
                        val networkReturnResult = (it as NetworkState.Success).data
                        _loginUiEvent.value = if (networkReturnResult.success && it.data.data != null) {
                            LoginUiEvent.Success
                        } else {
                            LoginUiEvent.Error(it.data.message)
                        }
                        errorMsg = if (!networkReturnResult.success) networkReturnResult.message else errorMsg
                    }
                    is NetworkState.Error -> {
                        _loginUiEvent.value = LoginUiEvent.Error(errorMsg = if (it.e.message == null) "" else it.e.message!!)
                        errorMsg = if (it.e.message != null) it.e.message!! else errorMsg
                    }
                    is NetworkState.Loading -> {
                        _loginUiEvent.value = LoginUiEvent.Loading
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
    val username: String = "",
    val password: String = "",
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