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
import com.example.madproject.sampledata.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = DatabaseInstance.getDatabase(context)

    // State variables for user input
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // State variables for password visibility
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // State variables for validation errors
    var firstNameError by remember { mutableStateOf(false) }
    var lastNameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }
    var signupError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Validation functions
    fun isValidName(name: String): Boolean = name.length >= 2
    fun isValidPhone(phone: String): Boolean = phone.length >= 10
    fun isValidUsername(username: String): Boolean = username.length >= 3
    fun isValidPassword(password: String): Boolean = password.length >= 8
    fun doPasswordsMatch(): Boolean = password == confirmPassword

    fun validateFields(): Boolean {
        firstNameError = !isValidName(firstName)
        lastNameError = !isValidName(lastName)
        phoneError = !isValidPhone(phone)
        usernameError = !isValidUsername(username)
        passwordError = !isValidPassword(password)
        confirmPasswordError = !doPasswordsMatch()

        return !firstNameError && !lastNameError && !phoneError &&
                !usernameError && !passwordError && !confirmPasswordError
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Account") },
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // First Name
            OutlinedTextField(
                value = firstName,
                onValueChange = {
                    firstName = it
                    firstNameError = false
                },
                label = { Text("First Name") },
                isError = firstNameError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )
            if (firstNameError) {
                Text(
                    text = "First name must be at least 2 characters",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Last Name
            OutlinedTextField(
                value = lastName,
                onValueChange = {
                    lastName = it
                    lastNameError = false
                },
                label = { Text("Last Name") },
                isError = lastNameError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth()
            )
            if (lastNameError) {
                Text(
                    text = "Last name must be at least 2 characters",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    phoneError = false
                },
                label = { Text("Phone Number") },
                isError = phoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            if (phoneError) {
                Text(
                    text = "Please enter a valid phone number",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    usernameError = false
                    signupError = false
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

            Spacer(modifier = Modifier.height(8.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = false
                    confirmPasswordError = false
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

            Spacer(modifier = Modifier.height(8.dp))

            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = false
                },
                label = { Text("Confirm Password") },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                isError = confirmPasswordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            if (confirmPasswordError) {
                Text(
                    text = "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Button
            Button(
                onClick = {
                    if (validateFields()) {
                        scope.launch {
                            try {
                                // Check if username already exists
                                val existingUser = withContext(Dispatchers.IO) {
                                    database.userDao().getUserByUsername(username)
                                }

                                if (existingUser != null) {
                                    signupError = true
                                    errorMessage = "Username already exists"
                                } else {
                                    // Create new user
                                    val newUser = User(
                                        userId = UUID.randomUUID().toString(),
                                        firstName = firstName,
                                        lastName = lastName,
                                        phone = phone,
                                        username = username,
                                        password = password,
                                        latitude = 0.0,  // Default values
                                        longitude = 0.0, // Default values
                                        timestamp = System.currentTimeMillis()
                                    )

                                    withContext(Dispatchers.IO) {
                                        database.userDao().insertUser(newUser)
                                    }

                                    // Navigate to login screen
                                    navController.navigate(Screen.LoginScreen.route) {
                                        popUpTo(Screen.SignupScreen.route) { inclusive = true }
                                    }
                                }
                            } catch (e: Exception) {
                                signupError = true
                                errorMessage = "Error: ${e.localizedMessage}"
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Up")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Navigation
            TextButton(
                onClick = { navController.navigate(Screen.LoginScreen.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Already have an account? Login")
            }

            // Error Message
            if (signupError) {
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