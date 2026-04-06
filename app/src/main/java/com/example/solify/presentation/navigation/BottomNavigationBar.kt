package com.example.solify.presentation.navigation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.solify.R
import com.example.solify.presentation.ui.theme.Brown300
import com.example.solify.presentation.ui.theme.Grey300
import com.example.solify.presentation.ui.theme.White300

@Composable
fun BottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem.Training,
        BottomNavItem.Main,
        BottomNavItem.Profile
    )

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .systemBarsPadding(),
        containerColor = Brown300,
        tonalElevation = 10.dp,
        windowInsets = WindowInsets()
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Training.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(item.iconResId),
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,

                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedTextColor = White300,
                    unselectedTextColor = Grey300,
                    selectedIconColor = White300,
                    unselectedIconColor = Grey300,
                    indicatorColor = Color.Transparent
                ),
                interactionSource = remember { MutableInteractionSource() }
            )
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val iconResId: Int,
) {
    object Training : BottomNavItem(
        route = Screen.Training.route,
        title = "Training",
        iconResId = R.drawable.training_icon_active,
    )
    object Main : BottomNavItem(
        route = Screen.Main.route,
        title = "Main",
        iconResId = R.drawable.lesson_icon_active,
    )
    object Profile : BottomNavItem(
        route = Screen.Profile.route,
        title = "Profile",
        iconResId = R.drawable.profile_icon_active,
    )
}