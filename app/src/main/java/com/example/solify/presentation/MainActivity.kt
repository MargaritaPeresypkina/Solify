package com.example.solify.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.solify.presentation.navigation.NavGraph
import com.example.solify.presentation.ui.theme.SolifyTheme
import com.example.solify.presentation.utils.hideSystemNavigationBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        hideSystemNavigationBar(window)

        val viewModel = mainViewModel
        setContent {
            SolifyTheme {
               NavGraph(mainViewModel = viewModel)
            }
        }
    }
}