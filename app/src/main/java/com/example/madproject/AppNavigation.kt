package com.example.madproject

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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
            ViewTripsScreen(navController, viewMyTrips = true)
        }
        composable(Screen.ViewOtherTripsScreen.route) {
            ViewTripsScreen(navController, viewMyTrips = false)
        }
        composable(Screen.UserListScreen.route) {
            UserListScreen(navController)
        }
    }
}