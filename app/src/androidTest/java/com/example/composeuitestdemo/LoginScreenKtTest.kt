package com.example.composeuitestdemo

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyPress
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.delay
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenKtTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    @get:Rule
    val mockkRule = MockKRule(this)
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
        // 初始化页面
        composeTestRule.setContent {
            LoginScreen(
                modifier = Modifier,
                loginSuccess = loginSuccess
            )
        }
        // 通过在 composable 中的 modifier 设置 testTag 属性
        loginField = composeTestRule.onNodeWithTag("loginUsernameTextField")
        passwordField = composeTestRule.onNodeWithTag("loginPasswordTextField")
        loginButton = composeTestRule.onNodeWithTag("loginLoginButton")
        errorText = composeTestRule.onNodeWithTag("loginErrorText")
        loginLoading = composeTestRule.onNodeWithTag("loginLoading")

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
    }

    @After
    fun after() {
        unmockkAll()
    }

    @Test
    fun testLoginButtonLogin() {
        loginField.performTextClearance()
        passwordField.performTextClearance()

        loginField.performTextInput("username")
        passwordField.performTextInput("password")
        loginButton.assertIsEnabled()
        loginButton.performClick()

        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("loginLoading").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("loginLoading").fetchSemanticsNodes().isEmpty()
        }
        verify {
            loginSuccess.invoke()
        }
    }

    @Test
    fun testKeyDoneLogin() {
        loginField.performTextClearance()
        passwordField.performTextClearance()
        loginField.performTextInput("username")
        passwordField.performTextInput("password")
        loginButton.assertIsEnabled()
        val nativeKeyEvent = NativeKeyEvent(
            NativeKeyEvent.ACTION_DOWN,  // 使用 ACTION_UP 表示键被抬起
            NativeKeyEvent.KEYCODE_ENTER // 使用 KEYCODE_ENTER 来模拟 Enter 键 (通常与 Done 键等价)
        )
        passwordField.performKeyPress(androidx.compose.ui.input.key.KeyEvent(nativeKeyEvent))

        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("loginLoading").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag("loginLoading").fetchSemanticsNodes().isEmpty()
        }
        verify {
            loginSuccess.invoke()
        }
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
