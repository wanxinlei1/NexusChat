package com.aichat.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aichat.app.ui.screens.chat.ChatScreen
import com.aichat.app.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Settings : Screen("settings")
    object Chat : Screen("chat")
}

@Composable
fun AIChatNavigation(
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val startDestination by viewModel.startDestination.collectAsState()

    val initialDestination = startDestination ?: Screen.Settings.route

    NavHost(
        navController = navController,
        startDestination = initialDestination
    ) {
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToChat = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Settings.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
    }
}
