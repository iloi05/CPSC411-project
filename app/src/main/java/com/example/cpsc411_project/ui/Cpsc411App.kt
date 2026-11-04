package com.example.cpsc411_project.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cpsc411_project.ui.screens.AuthScreen
import com.example.cpsc411_project.ui.screens.GameScreen
import com.example.cpsc411_project.ui.screens.LoadingScreen
import com.example.cpsc411_project.ui.screens.SettingsScreen

object Routes {
    const val AUTH = "auth"
    const val LOADING = "loading"
    const val GAME = "game"
    const val SETTINGS = "settings"
}

@Composable
fun Cpsc411App() {
    val context = LocalContext.current

    // Check user state to determine where the app should start
    val userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val petPrefs = context.getSharedPreferences("pet", Context.MODE_PRIVATE)

    val isLoggedIn = userPrefs.getBoolean("isLoggedIn", false)
    val hasPet = petPrefs.contains("pet_type")

    // Determine start destination based on memory
    val startDestination = when {
        !isLoggedIn -> Routes.AUTH
        isLoggedIn && !hasPet -> Routes.LOADING
        else -> Routes.GAME
    }

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 1. Login / Sign up Screen
        composable(Routes.AUTH) {
            AuthScreen(
                onAuthSuccess = {
                    // Check if they already have a pet. If so, skip loading screen.
                    val nextRoute = if (petPrefs.contains("pet_type")) Routes.GAME else Routes.LOADING

                    navController.navigate(nextRoute) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // 2. Pet Picker Screen
        composable(Routes.LOADING) {
            LoadingScreen(
                onContinue = {
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.LOADING) { inclusive = true }
                    }
                }
            )
        }

        // 3. Game Screen
        composable(Routes.GAME) {
            GameScreen(
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        // 4. Settings Screen
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onRepickPet = {
                    navController.navigate(Routes.LOADING) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onLogout = {
                    // 1. Clear the login state
                    userPrefs.edit(commit = true) { putBoolean("isLoggedIn", false) }

                    // 2. Navigate back to login and clear the entire backstack
                    navController.navigate(Routes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}