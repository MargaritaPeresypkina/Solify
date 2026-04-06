package com.example.solify.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.solify.presentation.ui.theme.Grey200
import com.example.solify.presentation.ui.theme.Grey300
import com.example.solify.presentation.ui.theme.Red100

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    isError: Boolean = false
) {
    val visualTransformation = remember(isPassword) {
        if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                placeholder,
                style = MaterialTheme.typography.labelMedium,
                color = Grey300
            )
        },
        visualTransformation = visualTransformation,
        isError = isError,
        shape = RoundedCornerShape(20.dp),
        colors = TextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
            cursorColor = MaterialTheme.colorScheme.onPrimary,
            focusedLabelColor = Grey300,
            unfocusedLabelColor = Grey300,
            errorCursorColor = Red100,
            errorTextColor = Red100,
            errorSupportingTextColor = Red100,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            focusedIndicatorColor = Grey200,
            errorIndicatorColor = Grey200,
            unfocusedIndicatorColor = Grey200,
            disabledIndicatorColor = Grey200
        ),
        singleLine = true
    )
}

