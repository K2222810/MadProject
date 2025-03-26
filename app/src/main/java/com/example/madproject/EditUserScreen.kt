package com.example.madproject

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madproject.sampledata.DatabaseInstance
import com.example.madproject.sampledata.User
import com.example.madproject.sampledata.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "EditUserScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(
    navController: NavController,
    userId: String
) {
    // Check for empty userId
    val context = LocalContext.current

    if (userId.isEmpty()) {
        LaunchedEffect(Unit) {
            Log.e(TAG, "Empty userId provided to EditUserScreen")
            Toast.makeText(context, "Error: Invalid user ID", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
        return
    }

    val database = DatabaseInstance.getDatabase(context)
    val scope = rememberCoroutineScope()

    // Check if current user is logged in
    val isLoggedIn = UserSessionManager.isLoggedIn.value
    val currentUserId = UserSessionManager.userId.value

    // Confirm dialogs
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // State for user data
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // State for UI
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var isAuthorized by remember { mutableStateOf(false) }

    // Validation states
    var firstNameError by remember { mutableStateOf(false) }
    var lastNameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }

    // Timestamp for existing user
    var timestamp by remember { mutableStateOf(0L) }

    // Location data for existing user
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }

    // Load user data
    LaunchedEffect(userId, currentUserId) {
        try {
            Log.d(TAG, "Starting to load user with ID: $userId")
            withContext(Dispatchers.IO) {
                try {
                    // Check if user is logged in
                    if (!isLoggedIn) {
                        loadError = "Please login to edit user information"
                        isLoading = false
                        return@withContext
                    }

                    // Fetch user data
                    val userToEdit = database.userDao().getAllUsers().find { it.userId == userId }

                    if (userToEdit != null) {
                        // Check authorization - Only allow users to edit their own account
                        isAuthorized = userId == currentUserId

                        if (!isAuthorized) {
                            loadError = "You are not authorized to edit this user"
                            isLoading = false
                            return@withContext
                        }

                        // Populate form with user data
                        userToEdit.firstName?.let { firstName = it }
                        userToEdit.lastName?.let { lastName = it }
                        userToEdit.phone?.let { phone = it }
                        userToEdit.username?.let { username = it }
                        userToEdit.password?.let { password = it }
                        confirmPassword = password

                        // Save location and timestamp
                        userToEdit.latitude?.let { latitude = it }
                        userToEdit.longitude?.let { longitude = it }
                        userToEdit.timestamp?.let { timestamp = it }

                        Log.d(TAG, "Loaded user data for: $firstName $lastName")
                    } else {
                        loadError = "User not found"
                        Log.e(TAG, "User not found with ID: $userId")
                    }
                } catch (e: Exception) {
                    loadError = "Error loading user data: ${e.localizedMessage}"
                    Log.e(TAG, "Error loading user: ${e.message}", e)
                } finally {
                    isLoading = false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in LaunchedEffect: ${e.message}", e)
            Toast.makeText(
                context,
                "An unexpected error occurred: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
            navController.popBackStack()
        }
    }

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

    // Function to update user
    fun updateUser() {
        if (!isLoggedIn || !isAuthorized) {
            Toast.makeText(context, "Not authorized to edit this user", Toast.LENGTH_LONG).show()
            return
        }

        if (validateFields()) {
            scope.launch {
                try {
                    // Check if username already exists and belongs to another user
                    val existingUser = withContext(Dispatchers.IO) {
                        database.userDao().getUserByUsername(username)
                    }

                    if (existingUser != null && existingUser.userId != userId) {
                        Toast.makeText(context, "Username already exists", Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    // Create updated user object
                    val updatedUser = User(
                        userId = userId,
                        firstName = firstName,
                        lastName = lastName,
                        phone = phone,
                        username = username,
                        password = password,
                        latitude = latitude,
                        longitude = longitude,
                        timestamp = timestamp
                    )

                    // Update user in database
                    withContext(Dispatchers.IO) {
                        database.userDao().insertUser(updatedUser)
                    }

                    // If current user is updated, update session
                    if (userId == currentUserId) {
                        UserSessionManager.startSession(updatedUser)
                    }

                    Toast.makeText(context, "User updated successfully", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()

                } catch (e: Exception) {
                    Log.e(TAG, "Error updating user: ${e.message}", e)
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Function to delete the account
    fun deleteAccount() {
        if (!isLoggedIn || !isAuthorized) {
            Toast.makeText(context, "Not authorized to delete this account", Toast.LENGTH_LONG).show()
            return
        }

        scope.launch {
            try {
                // Step 1: Delete all contacts associated with this user
                withContext(Dispatchers.IO) {
                    val allContacts = database.contactDao().getAllContacts()

                    // Delete contacts where this user is either the owner or the contact
                    val contactsToDelete = allContacts.filter {
                        it.userId == userId || it.contactUserId == userId
                    }

                    for (contact in contactsToDelete) {
                        database.contactDao().deleteContactById(contact.contactId)
                    }

                    Log.d(TAG, "Deleted ${contactsToDelete.size} contacts for user $userId")

                    // Step 2: Delete all friend requests associated with this user
                    try {
                        val friendRequestDao = database.friendRequestDao()
                        val allRequests = friendRequestDao.getAllFriendRequests()
                        val requestsToDelete = allRequests.filter {
                            it.senderId == userId || it.receiverId == userId
                        }

                        for (request in requestsToDelete) {
                            friendRequestDao.deleteFriendRequest(request.requestId)
                        }

                        Log.d(TAG, "Deleted ${requestsToDelete.size} friend requests for user $userId")
                    } catch (e: Exception) {
                        // If friend request table doesn't exist, just log and continue
                        Log.w(TAG, "Couldn't delete friend requests, may not exist: ${e.message}")
                    }

                    // Step 3: Delete all activities created by this user
                    val activitiesToDelete = database.activityDao().getAllActivities()
                        .filter { it.userId == userId }

                    for (activity in activitiesToDelete) {
                        // Also delete associated locations and statuses
                        try {
                            database.locationDao().deleteLocationById(activity.fromLocationId)
                            database.locationDao().deleteLocationById(activity.toLocationId)
                            database.statusDao().deleteStatusById(activity.statusId)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error deleting activity related data: ${e.message}")
                        }

                        database.activityDao().deleteActivityById(activity.activityId)
                    }

                    Log.d(TAG, "Deleted ${activitiesToDelete.size} activities for user $userId")

                    // Step 4: Finally, delete the user
                    database.userDao().deleteUserById(userId)
                    Log.d(TAG, "Deleted user $userId")
                }

                // Step 5: Log out the user if they deleted their own account
                if (userId == currentUserId) {
                    UserSessionManager.endSession()
                }

                Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_LONG).show()

                // Navigate back to login screen
                navController.navigate(Screen.LoginScreen.route) {
                    popUpTo(0) { inclusive = true }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error deleting account: ${e.message}", e)
                Toast.makeText(context, "Error deleting account: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Delete account confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Account") },
            text = {
                Text("Are you sure you want to delete your account? This action cannot be undone and will delete all your data including trips, contacts, and messages.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete My Account")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !isLoggedIn -> {
                    // Not logged in
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Login Required",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "You need to login to edit user information",
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { navController.navigate(Screen.LoginScreen.route) }) {
                            Text("Go to Login")
                        }
                    }
                }
                isLoading -> {
                    // Loading state
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                loadError != null || !isAuthorized -> {
                    // Error or unauthorized
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (!isAuthorized) "Not Authorized" else "Error",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = loadError ?: "You cannot edit this user",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Go Back")
                        }
                    }
                }
                else -> {
                    // Edit form
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
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

                        // Update button
                        Button(
                            onClick = { updateUser() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Update Profile")
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Delete Account Button
                        OutlinedButton(
                            onClick = { showDeleteConfirmation = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Account"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete My Account", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}