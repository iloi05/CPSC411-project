package com.example.cpsc411_project.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cpsc411_project.ui.screens.AuthScreen
import com.example.cpsc411_project.ui.screens.GameScreen
import com.example.cpsc411_project.ui.screens.LoadingScreen
import com.example.cpsc411_project.ui.screens.SettingsScreen

object Routes {
    const val AUTH = "auth"         // <--- New Route
    const val LOADING = "loading"
    const val GAME = "game"
    const val SETTINGS = "settings"
}

@Composable
fun Cpsc411App() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.AUTH // <--- Changed to start at AUTH
    ) {
        // 1. Login / Sign up Screen
        composable(Routes.AUTH) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Routes.LOADING) {
                        // Remove Auth from the backstack so they can't press "Back" to return
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
                }
            )
        }
    }
}