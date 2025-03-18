package com.example.madproject

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object AddTripsScreen : Screen("add_trip_screen")
}