package com.example.composeuitestdemo


object ApiServiceImpl: ApiService {

    private val USERNAME = "username"
    private val PASSWORD = "password"

    override suspend fun login(username: String, password: String): NetworkReturnResult<LoginResult<*>> {
        val result =  when (validCredentials(username, password)) {
            // 用户名错误响应数据
            MockLoginResult.UsernameError -> {
                NetworkReturnResult(
                    success = false,
                    message = "User Not Found!!",
                    data = LoginResult(
                        error = Any(),
                        path = "/api/users/v1/login",
                        timestamp = System.currentTimeMillis()
                    ),
                    errorCode = 20001
                )
            }
            // 密码错误响应数据
            MockLoginResult.PasswordError -> {
                NetworkReturnResult(
                    success = false,
                    message = "Password mismatch",
                    data = LoginResult(
                        error = Any(),
                        path = "/api/users/v1/login",
                        timestamp = System.currentTimeMillis()
                    ),
                    errorCode = 1004
                )
            }
            // 登录成功响应数据
            MockLoginResult.Success -> {
                NetworkReturnResult(
                    success = true,
                    message = "Login success",
                    data = LoginResult<Any>(
                        username = username
                    )
                )
            }
        }

        return result
    }

    enum class MockLoginResult{
        UsernameError,
        PasswordError,
        Success,
    }

    // 模拟用户名和密码的验证逻辑
    private fun validCredentials(username: String, password: String): MockLoginResult {
        return if(username != USERNAME) MockLoginResult.UsernameError
        else if (password != PASSWORD) MockLoginResult.PasswordError
        else MockLoginResult.Success
    }
}