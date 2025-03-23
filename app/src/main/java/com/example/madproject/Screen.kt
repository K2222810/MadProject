package com.example.madproject

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object AddTripsScreen : Screen("add_trip_screen")
    object LoginScreen : Screen("add_login_screen")
    object SignupScreen : Screen("add_Signup_screen")
    object ViewMyTripsScreen : Screen("view_my_trips_screen")
    object ViewOtherTripsScreen : Screen("view_other_trips_screen")
}