package com.aichat.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aichat.app.ui.screens.chat.ChatScreen
import com.aichat.app.ui.screens.history.HistoryScreen
import com.aichat.app.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Settings : Screen("settings")
    object Chat : Screen("chat?conversationId={conversationId}") {
        fun createRoute(conversationId: String? = null): String {
            return if (conversationId != null) "chat?conversationId=$conversationId" else "chat"
        }
    }
    object History : Screen("history")
}

@Composable
fun NexusChatNavigation(
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
                    navController.navigate(Screen.Chat.createRoute()) {
                        popUpTo(Screen.Settings.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId")
                ?.ifBlank { null }
            ChatScreen(
                conversationId = conversationId,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateToChat = { conversationId ->
                    navController.navigate(Screen.Chat.createRoute(conversationId)) {
                        popUpTo(Screen.History.route) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
