package com.example.madproject.sampledata

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

/**
 * Singleton class to manage user session information across the app
 */
object UserSessionManager {
    // User session states
    private val _isLoggedIn = mutableStateOf(false)
    val isLoggedIn: State<Boolean> = _isLoggedIn

    private val _userId = mutableStateOf("")
    val userId: State<String> = _userId

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _firstName = mutableStateOf("")
    val firstName: State<String> = _firstName

    private val _lastName = mutableStateOf("")
    val lastName: State<String> = _lastName

    /**
     * Start a user session after successful login
     */
    fun startSession(user: User) {
        user.userId.let { _userId.value = it }
        user.username?.let { _username.value = it }
        user.firstName?.let { _firstName.value = it }
        user.lastName?.let { _lastName.value = it }
        _isLoggedIn.value = true
    }

    /**
     * End the current user session (logout)
     */
    fun endSession() {
        _userId.value = ""
        _username.value = ""
        _firstName.value = ""
        _lastName.value = ""
        _isLoggedIn.value = false
    }

    /**
     * Check if a user is authenticated
     */
    fun requireAuthentication(): Boolean {
        return _isLoggedIn.value && _userId.value.isNotEmpty()
    }
}