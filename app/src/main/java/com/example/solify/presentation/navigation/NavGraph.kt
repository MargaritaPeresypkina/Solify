package com.example.solify.presentation.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.solify.domain.usecases.auth.AuthState
import com.example.solify.presentation.MainViewModel
import com.example.solify.presentation.screens.auth_choice.AuthChoiceScreen
import com.example.solify.presentation.screens.edit_profile.EditProfileScreen
import com.example.solify.presentation.screens.lesson.LessonScreen
import com.example.solify.presentation.screens.exercise.ExerciseScreen
import com.example.solify.presentation.screens.test.TestScreen
import com.example.solify.presentation.screens.theory.TheoryScreen
import com.example.solify.presentation.screens.lessons.LessonsScreen
import com.example.solify.presentation.screens.login.LoginScreen
import com.example.solify.presentation.screens.profile.ProfileScreen
import com.example.solify.presentation.screens.your_progress.YourProgressScreen
import com.example.solify.presentation.screens.register.RegisterScreen
import com.example.solify.presentation.screens.trainers.TrainersScreen
import com.example.solify.presentation.screens.trainings.TrainingScreen

@Composable
fun NavGraph(
    mainViewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    val authState by mainViewModel.authState.collectAsStateWithLifecycle()

    val startDestination = remember(authState) {
        when (authState) {
            is AuthState.Authorized -> Screen.MainTabs.route
            else -> Screen.AuthChoice.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { navEnterTransition() },
        exitTransition = { navExitTransition() },
        popEnterTransition = { navPopEnterTransition() },
        popExitTransition = { navPopExitTransition() }
    ) {

        composable(Screen.AuthChoice.route) {
            AuthChoiceScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.MainTabs.route) {
                        popUpTo(Screen.AuthChoice.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.MainTabs.route) {
                        popUpTo(Screen.AuthChoice.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        navigation(
            route = Screen.MainTabs.route,
            startDestination = Screen.Profile.route
        ) {
            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    onLogoutComplete = {
                        navController.popBackStack(Screen.MainTabs.route, inclusive = true)
                        navController.navigate(Screen.AuthChoice.route) {
                            popUpTo(Screen.AuthChoice.route) { inclusive = true }
                        }
                    },
                    onEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    }
                )
            }

            composable(Screen.Training.route) {
                TrainingScreen(
                    navController = navController,
                    onTrainingClick = { trainingId ->
                        navController.navigate(Screen.Trainers.createRoute(trainingId))
                    }
                )
            }

            composable(Screen.Lessons.route) {
                LessonsScreen(
                    navController = navController,
                    onLessonClick = { lessonId ->
                        navController.navigate(Screen.Lesson.createRoute(lessonId))
                    }
                )
            }
        }

        composable(route = Screen.Trainers.route) {
            val trainingId = Screen.Trainers.getTrainingId(it.arguments)
            TrainersScreen(
                trainingId = trainingId,
                onNavigateBack = { navController.popBackStack() },
                onTrainerClick = { trainerId ->
                    navController.navigate(Screen.Exercise.createRoute(trainerId))
                }
            )
        }

        composable(route = Screen.Exercise.route) {
            ExerciseScreen(
                onNavigateBack = { navController.popBackStack() },
                onSessionCompleted = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Lesson.route) {
            val lessonId = Screen.Lesson.getLessonId(it.arguments)
            LessonScreen(
                lessonId = lessonId,
                onNavigateBack = { navController.popBackStack() },
                onTheoryClick = { theoryItemId ->
                    navController.navigate(Screen.Theory.createRoute(theoryItemId))
                },
                onTestClick = { testId ->
                    navController.navigate(Screen.Test.createRoute(lessonId, testId))
                }
            )
        }

        composable(route = Screen.Test.route) {
            TestScreen(
                onNavigateBack = { navController.popBackStack() },
                onTestCompleted = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Theory.route) {
            TheoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onDeleteComplete = {
                    navController.navigate(Screen.AuthChoice.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.YourProgress.route) {
            YourProgressScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    data object AuthChoice : Screen("auth_choice")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object MainTabs : Screen("main_tabs")
    data object Profile : Screen("profile")
    data object EditProfile : Screen("edit_profile")
    data object YourProgress : Screen("your_progress")
    data object Training : Screen("training")
    data object Trainers : Screen("trainers/{training_id}") {
        fun createRoute(trainingId: String): String = "trainers/$trainingId"
        fun getTrainingId(arguments: Bundle?): String =
            arguments?.getString("training_id").orEmpty()
    }
    data object Lessons : Screen("lessons")
    data object Lesson : Screen("lesson/{lesson_id}") {
        fun createRoute(lessonId: String): String = "lesson/$lessonId"
        fun getLessonId(arguments: Bundle?): String =
            arguments?.getString("lesson_id").orEmpty()
    }

    data object Theory : Screen("theory/{theory_item_id}") {
        fun createRoute(theoryItemId: String): String = "theory/$theoryItemId"
        fun getTheoryItemId(arguments: Bundle?): String =
            arguments?.getString("theory_item_id").orEmpty()
    }

    data object Test : Screen("test/{lesson_id}/{test_id}") {
        fun createRoute(lessonId: String, testId: String): String = "test/$lessonId/$testId"
        fun getLessonId(arguments: Bundle?): String =
            arguments?.getString("lesson_id").orEmpty()
        fun getTestId(arguments: Bundle?): String =
            arguments?.getString("test_id").orEmpty()
    }

    data object Exercise : Screen("exercise/{trainer_id}") {
        fun createRoute(trainerId: String): String = "exercise/$trainerId"
        fun getTrainerId(arguments: Bundle?): String =
            arguments?.getString("trainer_id").orEmpty()
    }
}