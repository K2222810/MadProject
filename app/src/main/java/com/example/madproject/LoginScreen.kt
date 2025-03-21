package com.example.madproject

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.madproject.sampledata.DatabaseInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = DatabaseInstance.getDatabase(context)

    // State variables
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Validate username format
    fun isValidUsername(username: String): Boolean {
        return username.length >= 3
    }

    fun validateFields(): Boolean {
        usernameError = !isValidUsername(username)
        passwordError = password.length < 8

        return !usernameError && !passwordError
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("StaySafe Login") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Username Input
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    usernameError = false
                    loginError = false
                },
                label = { Text("Username") },
                isError = usernameError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )
            if (usernameError) {
                Text(
                    text = "Username must be at least 3 characters",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = false
                    loginError = false
                },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                isError = passwordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            if (passwordError) {
                Text(
                    text = "Password must be at least 8 characters",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = {
                    if (validateFields()) {
                        scope.launch {
                            loginError = false
                            try {
                                val user = withContext(Dispatchers.IO) {
                                    database.userDao().getUserByUsername(username)
                                }

                                if (user != null && user.password == password) {
                                    // Login successful
                                    // TODO: Save user session data as needed

                                    // Navigate to main screen
                                    navController.navigate(Screen.MainScreen.route) {
                                        // Clear back stack so user can't go back to login
                                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                                    }
                                } else {
                                    loginError = true
                                    errorMessage = "Invalid username or password"
                                }
                            } catch (e: Exception) {
                                loginError = true
                                errorMessage = "Error: ${e.localizedMessage}"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Up Navigation
            TextButton(
                onClick = { navController.navigate(Screen.SignupScreen.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Don't have an account? Sign Up")
            }

            // Error Message
            if (loginError) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}