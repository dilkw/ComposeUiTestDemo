package com.example.composeuitestdemo

import android.util.Log
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class LoginScreenKtTest {
    @get:Rule
    val composeTestRule = createComposeRule()
//    @get:Rule
//    val mockkRule = MockKRule(this)

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var loginRepository: LoginRepository

    private lateinit var loginField: SemanticsNodeInteraction
    private lateinit var passwordField: SemanticsNodeInteraction
    private lateinit var loginButton: SemanticsNodeInteraction
    private lateinit var errorText: SemanticsNodeInteraction
    private lateinit var loginLoading: SemanticsNodeInteraction

    private val USERNAME = "username"
    private val PASSWORD = "password"

    private var username = slot<String>()
    private var password = slot<String>()
    private val loginSuccess = mockk<() -> Unit>(relaxed = true)

    @Before
    fun loadScreen() {
        mockkObject(ApiServiceImpl)
    }

    @After
    fun after() {
        unmockkAll()
    }

    @Test
    fun testLoginButtonLogin() = runTest(timeout = 65.seconds) {
        val dispatcher = StandardTestDispatcher(testScheduler)
        loginRepository = LoginRepository(defaultDispatcher = dispatcher, apiService = ApiServiceImpl)
        loginViewModel = LoginViewModel(defaultDispatcher = dispatcher, loginRepository = loginRepository)

        // 初始化页面
        composeTestRule.setContent {
            LoginScreen(
                modifier = Modifier,
                loginViewModel = loginViewModel,
                loginSuccess = loginSuccess
            )
        }
        // 通过在 composable 中的 modifier 设置 testTag 属性
        loginField = composeTestRule.onNodeWithTag("loginUsernameTextField")
        passwordField = composeTestRule.onNodeWithTag("loginPasswordTextField")
        loginButton = composeTestRule.onNodeWithTag("loginLoginButton")
        errorText = composeTestRule.onNodeWithTag("loginErrorText")
        loginLoading = composeTestRule.onNodeWithTag("loginLoading")

        loginField.performTextClearance()
        passwordField.performTextClearance()
        loginField.performTextInput("username")
        passwordField.performTextInput("password")
        loginButton.assertIsEnabled()
        loginButton.performClick()

        //定义条件和满足条件的返回值
        coEvery {
            ApiServiceImpl.login(capture(username), capture(password))
        } coAnswers {
            delay(4000)
            when (validCredentials(username.captured, password.captured)) {
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
                            username = username.captured
                        )
                    )
                }
            }
        }
        Log.d("TAG", "testLoginButtonLogin111: ")
        // 等待 IdlingResource 变为空闲状态
        composeTestRule.awaitIdle()
        verify(exactly = 1, timeout = 60000) {
            loginSuccess()
        }
        composeTestRule.waitForIdle()
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