package com.example.madproject

sealed class Screen(val route: String) {
    object MainScreen : Screen("counter_screen")
}