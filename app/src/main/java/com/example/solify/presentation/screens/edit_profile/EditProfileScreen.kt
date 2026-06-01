package com.example.solify.presentation.screens.edit_profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.solify.presentation.components.skeleton.EditProfileScreenSkeleton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.solify.R
import com.example.solify.presentation.components.CustomTextField
import com.example.solify.presentation.ui.theme.Grey300
import com.example.solify.presentation.ui.theme.Red100

@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onDeleteComplete: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val onNameChange = remember {
        { name: String -> viewModel.processCommand(EditProfileCommand.OnNameChanged(name))
        } }
    val onSurnameChange = remember {
        { surname: String -> viewModel.processCommand(EditProfileCommand.OnSurnameChanged(surname))
        } }
    val onEmailChange = remember {
        { email: String -> viewModel.processCommand(EditProfileCommand.OnEmailChanged(email))
        } }

    LaunchedEffect(uiState.onNavigateBack) {
        if (uiState.onNavigateBack) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.accountDeleted) {
        if (uiState.accountDeleted) {
            onDeleteComplete()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.processCommand(EditProfileCommand.OnResetError)
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.processCommand(EditProfileCommand.OnUploadAvatar(it.toString()))
            }
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(top = innerPadding.calculateTopPadding())
                .padding(top = 20.dp)
        ) {
            if (uiState.isLoading && uiState.originalUser == null) {
                EditProfileScreenSkeleton(modifier = Modifier.fillMaxSize())
                return@Scaffold
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.cross),
                            contentDescription = stringResource(R.string.close),
                            modifier = Modifier
                                .size(12.dp)
                                .clickable {
                                    onNavigateBack()
                                },
                            tint = MaterialTheme.colorScheme.onPrimary
                        )

                        Text(
                            text = stringResource(R.string.save),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.clickable {
                                viewModel.processCommand(EditProfileCommand.OnSaveClick)
                            }
                        )
                    }
                    Text(
                        text = stringResource(R.string.edit_profile),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }


                Spacer(Modifier.height(24.dp))

                Column(
                    modifier = modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp, bottom = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(94.dp)
                                    .background(Grey300),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!uiState.avatarUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = uiState.avatarUrl,
                                        contentDescription = stringResource(R.string.avatar_image),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.PersonOutline,
                                        contentDescription = stringResource(R.string.avatar_icon),
                                        modifier = Modifier.size(47.dp),
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            if (uiState.avatarUrl != null) {
                                Box(modifier = Modifier
                                    .clip(CircleShape)
                                    .size(30.dp)
                                    .background(color = MaterialTheme.colorScheme.onSecondary)
                                    .clickable {
                                        viewModel.processCommand(EditProfileCommand.OnDeleteAvatar)
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.delete),
                                        contentDescription = stringResource(R.string.delete),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }


                        Spacer(Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.upload_photo),
                                color = MaterialTheme.colorScheme.onSecondary,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.clickable {
                                    imagePicker.launch("image/*")
                                }
                            )
                        }

                        Spacer(Modifier.height(32.dp))

                        CustomTextField(
                            value = uiState.name,
                            onValueChange = onNameChange,
                            placeholder = stringResource(R.string.name),
                            isError = uiState.nameError != null
                        )

                        if (uiState.nameError != null) {
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.TopStart
                            ) {
                                Text(
                                    text = uiState.nameError!!,
                                    color = Red100,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        CustomTextField(
                            value = uiState.surname,
                            onValueChange = onSurnameChange,
                            placeholder = stringResource(R.string.surname),
                            isError = uiState.surnameError != null
                        )

                        if (uiState.surnameError != null) {
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.TopStart
                            ) {
                                Text(
                                    text = uiState.surnameError!!,
                                    color = Red100,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        CustomTextField(
                            value = uiState.email,
                            onValueChange = onEmailChange,
                            placeholder = stringResource(R.string.email),
                            isError = uiState.emailError != null
                        )

                        if (uiState.emailError != null) {
                            Spacer(Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.TopStart
                            ) {
                                Text(
                                    text = uiState.emailError!!,
                                    color = Red100,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = stringResource(R.string.delete_account),
                                color = Red100,
                                style = MaterialTheme.typography.displaySmall,
                                modifier = Modifier.clickable {
                                    viewModel.processCommand(EditProfileCommand.OnDeleteAccountClick)
                                }
                            )
                        }
                    }
                }
            }

            if (uiState.showDeleteAccountDialog) {
                DeleteAccountDialog(
                    password = uiState.deletePassword,
                    passwordError = uiState.deletePasswordError,
                    onPasswordChange = { viewModel.processCommand(EditProfileCommand.OnUpdateDeletePassword(it)) },
                    onConfirm = {
                        viewModel.processCommand(EditProfileCommand.OnConfirmDeleteAccount(uiState.deletePassword))
                    },
                    onDismiss = {
                        viewModel.processCommand(EditProfileCommand.OnDismissDeleteDialog)
                    }
                )
            }
            if (uiState.showPasswordDialog) {
                AlertDialog(
                    onDismissRequest = {
                        viewModel.processCommand(EditProfileCommand.OnDismissPasswordDialog)
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.confirm_password),
                            color = MaterialTheme.colorScheme.onSecondary,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = stringResource(R.string.enter_your_password_to_change_email_address),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp),
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                            CustomTextField(
                                value = uiState.pendingPassword,
                                onValueChange = {
                                    viewModel.processCommand(EditProfileCommand.OnUpdatePendingPassword(it))
                                },
                                placeholder = stringResource(R.string.enter_your_password),
                                isError = uiState.pendingPasswordError != null,
                                isPassword = true,
                                supportingText = {
                                    if (uiState.pendingPasswordError != null) {
                                        Text(
                                            text = uiState.pendingPasswordError!!,
                                            color = Red100,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.processCommand(
                                    EditProfileCommand.OnConfirmPassword(uiState.pendingPassword)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSecondary)
                        ) {
                            Text(
                                stringResource(R.string.confirm),
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                viewModel.processCommand(EditProfileCommand.OnDismissPasswordDialog)
                            }
                        ) {
                            Text(
                                stringResource(R.string.cancel),
                                color = MaterialTheme.colorScheme.onSecondary,
                                style = MaterialTheme.typography.displaySmall
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DeleteAccountDialog(
    password: String,
    passwordError: String?,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Account",
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.titleLarge,

        )},
        text = {
            Column {
                Text(
                    text = "Are you sure you want to delete your account? This action cannot be undone.\n\nEnter your password to confirm.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.onSecondary
                )
                CustomTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    placeholder = "Enter your password",
                    isError = passwordError != null,
                    isPassword = true,
                    supportingText = {
                        if (passwordError != null) {
                            Text(
                                text = passwordError,
                                color = Red100,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSecondary)
            ) {
                Text(
                    "Delete",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "Cancel",
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.displaySmall
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.primary
    )
}

