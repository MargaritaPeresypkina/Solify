package com.example.solify.presentation.screens.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.solify.presentation.components.CustomButton
import com.example.solify.presentation.components.CustomTextField

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && uiState.user != null) {
            onRegisterSuccess()
        }
    }

    val isFormValid by remember(uiState.name, uiState.surname, uiState.email, uiState.password) {
        derivedStateOf {
            uiState.name.isBlank() &&
            uiState.surname.isBlank() &&
            uiState.email.isBlank() &&
            uiState.password.isBlank()
        }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Создать аккаунт",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(20.dp))
                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 24.dp)
                        .padding(top = 40.dp, bottom = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ){
                        CustomTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.processCommand(RegisterCommand.OnEmailChanged(it)) },
                            placeholder = "Email",
                            isError = uiState.error?.contains("email", ignoreCase = true) == true
                        )
                        Spacer(Modifier.height(12.dp))
                        CustomTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.processCommand(RegisterCommand.OnPasswordChanged(it)) },
                            placeholder = "Password",
                            isPassword = true,
                            isError = uiState.error?.contains("password", ignoreCase = true) == true
                        )
                        Spacer(Modifier.height(12.dp))
                        CustomTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.processCommand(RegisterCommand.OnNameChanged(it)) },
                            placeholder = "Name"
                        )
                        Spacer(Modifier.height(12.dp))
                        CustomTextField(
                            value = uiState.surname,
                            onValueChange = { viewModel.processCommand(RegisterCommand.OnSurnameChanged(it)) },
                            placeholder = "Surname"
                        )
                        val errorMessage = uiState.error
                        if (errorMessage != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        Row{
                            Text(
                                "Уже есть аккаунт?",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.width(5.dp))

                            Text(
                                "Вход в аккаунт",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.displaySmall,
                                modifier = modifier.clickable{
                                    onNavigateToLogin()
                                }
                            )
                        }
                    }
                    if (isFormValid) {
                        CustomButton(
                            onButtonClick = { },
                            buttonName = "Зарегистрироваться",
                            buttonColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    } else {
                        CustomButton(
                            onButtonClick = { viewModel.processCommand(RegisterCommand.OnRegisterClick) },
                            buttonName = "Зарегистрироваться",
                            buttonColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}