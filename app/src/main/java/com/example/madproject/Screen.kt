package com.example.madproject

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object AddTripsScreen : Screen("add_trip_screen")
    object LoginScreen : Screen("add_login_screen")
    object SignupScreen : Screen("add_Signup_screen")
    object ViewMyTripsScreen : Screen("view_my_trips_screen")
    object ViewOtherTripsScreen : Screen("view_other_trips_screen")
    object UserListScreen : Screen("user_list_screen")
    object EditTripScreen : Screen("edit_trip_screen/{tripId}") {
        fun createRoute(tripId: String): String = "edit_trip_screen/$tripId"
    }
    object EditUserScreen : Screen("edit_user_screen/{userId}") {
        fun createRoute(userId: String): String = "edit_user_screen/$userId"
    }
    object AddUserScreen : Screen("add_user_screen")
    object MapScreen : Screen("map_screen")
    object FriendRequestsScreen : Screen("friend_requests_screen")  // Add this line
    object NotificationsScreen : Screen("notifications_screen")

}