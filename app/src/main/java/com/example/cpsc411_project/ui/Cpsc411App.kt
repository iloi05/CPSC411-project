package com.example.cpsc411_project.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cpsc411_project.ui.navigation.Routes
import com.example.cpsc411_project.ui.screens.GameScreen
import com.example.cpsc411_project.ui.screens.LoadingScreen
import com.example.cpsc411_project.ui.screens.SettingsScreen

@Composable
fun Cpsc411App() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOADING
    ) {
        composable(Routes.LOADING) {
            LoadingScreen(
                onContinue = {
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.LOADING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.GAME) {
            GameScreen(
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }

            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}