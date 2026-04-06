package com.example.solify.presentation.screens.auth_choice

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.solify.presentation.components.CustomButton

@Composable
fun AuthChoiceScreen(
   modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
        ){
            Text(
                "Добрый день!",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(30.dp))
            ChoiceField(
                modifier = modifier,
                question = "Уже есть аккаунт?",
                description = "Начните с того, на чём остановились",
                buttonName = "Войти",
                onButtonClick = {
                    onLoginClick()
                }
            )
            Spacer(modifier = Modifier.height(30.dp))
            ChoiceField(
                modifier = modifier,
                question = "Впервые с нами?",
                description = "Начните заниматься музыкой прямо сейчас",
                buttonName = "Начать",
                onButtonClick = {
                    onRegisterClick()
                }
            )
        }
    }
}

@Composable
fun ChoiceField(
    modifier: Modifier = Modifier,
    question: String,
    description: String,
    buttonName: String,
    onButtonClick: () -> Unit
){
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(26.dp)
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = question,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(18.dp))
            CustomButton(
                onButtonClick = onButtonClick,
                buttonName = buttonName,
                buttonColor = MaterialTheme.colorScheme.secondary
            )
        }
    }
}