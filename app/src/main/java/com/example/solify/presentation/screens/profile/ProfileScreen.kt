package com.example.solify.presentation.screens.profile

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import com.example.solify.presentation.components.skeleton.ProfileScreenSkeleton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.solify.R
import com.example.solify.presentation.navigation.BottomNavigationBar
import com.example.solify.presentation.ui.theme.Grey300

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogoutComplete: () -> Unit,
    onEditProfile: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = uiState.user

    if (user == null || !uiState.isProfileReady) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            }
        ) { innerPadding ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(top = innerPadding.calculateTopPadding())
                    .padding(top = 20.dp)
            ) {
                ProfileScreenSkeleton(modifier = Modifier.fillMaxSize())
            }
        }
        return
    }

    LaunchedEffect(uiState.isLoggedOut) {
        Log.d("launch", "launch")
        if (uiState.isLoggedOut) {
            onLogoutComplete()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.processCommand(ProfileCommand.UploadAvatarImage(it.toString()))
            }
        }
    )

    val avatarSize = 94

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(top = innerPadding.calculateTopPadding())
                .padding(top = 10.dp),
        ) {
            Column(
                modifier = modifier.fillMaxSize().padding(top = 16.dp)
            ) {
                Box(
                    modifier = modifier.zIndex(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp, top = 8.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.exit),
                            contentDescription = null,
                            modifier = Modifier
                                .height(18.dp)
                                .clickable{
                                    viewModel.processCommand(ProfileCommand.OnLogoutClick)
                                }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(avatarSize.dp)
                                .zIndex(1f)
                                .clickable {
                                    imagePicker.launch("image/*")
                                }
                        ) {

                            AvatarImage(
                                imageUrl = user.avatarUrl,
                                onClick = {
                                    imagePicker.launch("image/*")
                                },
                                size = avatarSize
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = (-(avatarSize/2)).dp)
                        .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row (
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Image(
                                painter = painterResource(uiState.userBadgeRes),
                                contentDescription = "user_badge",
                                modifier = modifier.height(32.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height((avatarSize/2 + 18).dp))
                            Text(
                                "${user.surname} ${user.name}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Edit profile",
                                color = MaterialTheme.colorScheme.onSecondary,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .clickable{
                                        onEditProfile()
                                    },
                            )
                            Spacer(Modifier.height(17.dp))
                            WeeklyActivityChart(
                                days = uiState.weeklyActivityDays,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarImage(
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Grey300)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {

        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.PersonOutline,
                contentDescription = "Avatar",
                modifier = Modifier.size((size * 0.5).dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
