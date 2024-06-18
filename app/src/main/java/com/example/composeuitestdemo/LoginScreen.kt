package com.example.composeuitestdemo

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController

@Composable
fun LoginScreen(
    modifier: Modifier,
    loginViewModel: LoginViewModel = viewModel(),
    loginSuccess: () -> Unit = {}
) {

    val loginUiEvent by loginViewModel.loginUiEvent.collectAsState()
    val context = LocalContext.current
    when (loginUiEvent) {
        is LoginUiEvent.Success -> {
            loginSuccess()
            Toast.makeText(context, "login success", Toast.LENGTH_SHORT).show()
        }
        else -> {}
    }
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.wrapContentSize(),
                text = "LoginPage",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(50.dp))
            TextField(
                modifier = Modifier.testTag("loginUsernameTextField"),
                value = loginViewModel.loginUiState.username,
                onValueChange = loginViewModel.loginUsernameTextOnChangeListener
            )
            TextField(
                modifier = Modifier.testTag("loginPasswordTextField"),
                value = loginViewModel.loginUiState.password,
                onValueChange = loginViewModel.loginPasswordTextOnChangeListener
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (loginViewModel.loginUiState.isError) {
                Text(
                    modifier = Modifier
                        .testTag("loginErrorText")
                        .padding(top = 4.dp),
                    text = loginViewModel.loginUiState.errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    loginViewModel.login()
                },
                modifier = Modifier
                    .testTag("loginLoginButton")
                    .fillMaxWidth()
                    .padding(horizontal = 54.dp)
            ) {
                Text(text = "登录")
            }


        }

    }

    if (loginViewModel.loginUiState.isShowLoading) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.5f),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text(text = "Loading")
            }
        }
    }

}

@Preview
@Composable
fun LoginScreenPreView() {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        LoginScreen(modifier = Modifier)
    }
}