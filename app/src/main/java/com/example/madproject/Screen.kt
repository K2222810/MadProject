package com.example.madproject

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object AddTripsScreen : Screen("add_trip_screen")
<<<<<<< Updated upstream
=======
    object AddMemberScreen : Screen("add_member_screen")
    object UserListScreen : Screen("user_list_screen")
<<<<<<< Updated upstream
<<<<<<< Updated upstream
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
    object LoginScreen : Screen("add_login_screen")
    object SignupScreen : Screen("add_Signup_screen")
}