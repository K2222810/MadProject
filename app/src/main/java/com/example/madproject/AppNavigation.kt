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
        startDestination = Screen.LoginScreen.route // Start with login screen
    ) {
        composable(Screen.MainScreen.route) {
            MainScreen(navController)
        }
        composable(Screen.AddTripsScreen.route) {
            AddTripsScreen(navController)
        }
<<<<<<< Updated upstream
=======
        composable(Screen.AddMemberScreen.route) {
            AddMemberScreen(navController)
        }
        composable(Screen.UserListScreen.route){
            UserListScreen(navController)
        }
>>>>>>> Stashed changes
        composable(Screen.LoginScreen.route) {
            LoginScreen(navController)
        }
        composable(Screen.SignupScreen.route) {
            SignupScreen(navController)
        }
    }
}