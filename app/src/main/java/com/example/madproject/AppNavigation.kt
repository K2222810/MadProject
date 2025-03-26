package com.example.madproject

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.LoginScreen.route
    ) {
        composable(Screen.MainScreen.route) {
            MainScreen(navController)
        }
        composable(Screen.AddTripsScreen.route) {
            AddTripsScreen(navController)
        }
        composable(Screen.LoginScreen.route) {
            LoginScreen(navController)
        }
        composable(Screen.SignupScreen.route) {
            SignupScreen(navController)
        }
        composable(Screen.ViewMyTripsScreen.route) {
            ViewTripsScreen(navController)
        }
        composable(Screen.ViewOtherTripsScreen.route) {
            ViewTripsScreen(navController)
        }
        composable(Screen.UserListScreen.route) {
            UserListScreen(navController)
        }
        // Edit Trip Screen with trip ID parameter
        composable(
            route = Screen.EditTripScreen.route,
            arguments = listOf(
                navArgument("tripId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            EditTripScreen(navController, tripId)
        }
        // Add User Screen
        composable(Screen.AddUserScreen.route) {
            AddUserScreen(navController)
        }
    }
}