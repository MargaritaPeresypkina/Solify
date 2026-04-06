package com.example.solify.presentation.navigation

import android.util.Log
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.solify.domain.usecases.auth.AuthState
import com.example.solify.presentation.MainViewModel
import com.example.solify.presentation.screens.auth_choice.AuthChoiceScreen
import com.example.solify.presentation.screens.lessons.LessonsScreen
import com.example.solify.presentation.screens.login.LoginScreen
import com.example.solify.presentation.screens.profile.ProfileScreen
import com.example.solify.presentation.screens.register.RegisterScreen
import com.example.solify.presentation.screens.trainings.TrainingScreen

@Composable
fun NavGraph(
    mainViewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    SideEffect {
        Log.d("NavGraph", "start")
    }


    val authState by mainViewModel.authState.collectAsStateWithLifecycle()

    SideEffect {
        Log.d("NavGraph", "$authState")
    }

    val startDestination = remember(authState) {
        when (authState) {
            is AuthState.Authorized -> Screen.Profile.route
            else -> Screen.AuthChoice.route
        }
    }
    SideEffect {
        Log.d("NavGraph", "startDestination $startDestination")
    }


    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {

        composable(Screen.AuthChoice.route) {
            SideEffect {
                Log.d("NavGraph ", "AuthChoice")
            }
            AuthChoiceScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Login.route) {
            SideEffect {
                Log.d("NavGraph ", "Login")
            }
            LoginScreen(
                onLoginSuccess = {
                    val route = Screen.Profile.route
                    navController.navigate(route) {
                        popUpTo(Screen.AuthChoice.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            SideEffect {
                Log.d("NavGraph ", "Register")
            }
            RegisterScreen(
                onRegisterSuccess = {
                    val route = Screen.Profile.route
                    navController.navigate(route) {
                        popUpTo(Screen.AuthChoice.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(Screen.Profile.route) {
            SideEffect {
                Log.d("NavGraph ", "Profile")
            }
            ProfileScreen(
                navController = navController,
                onLogoutComplete = {
                    navController.popBackStack(Screen.Profile.route, inclusive = true)
                    navController.navigate(Screen.AuthChoice.route) {
                        popUpTo(Screen.AuthChoice.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Training.route) {
            TrainingScreen(navController = navController)
        }

        composable(Screen.Main.route) {
            LessonsScreen(navController = navController)
        }
    }
}

sealed class Screen(val route: String) {
    data object AuthChoice : Screen("auth_choice")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Profile : Screen("profile")
    data object Training : Screen("training")
    data object Main : Screen("main")
}
