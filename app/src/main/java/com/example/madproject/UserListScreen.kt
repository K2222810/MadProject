package com.example.madproject

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madproject.sampledata.DatabaseInstance
import com.example.madproject.sampledata.User
import com.example.madproject.sampledata.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "UserListScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val database = DatabaseInstance.getDatabase(context)
    val scope = rememberCoroutineScope()

    // Check if current user is logged in
    val isLoggedIn = UserSessionManager.isLoggedIn.value
    val currentUserId = UserSessionManager.userId.value

    // State for users - we'll maintain two lists:
    // 1. allUsers - all users from the database
    // 2. displayedUsers - users currently displayed in the UI
    var allUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var displayedUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userToRemove by remember { mutableStateOf<User?>(null) }

    // Function to refresh users from database
    fun loadUsers() {
        isLoading = true
        errorMessage = null

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val users = database.userDao().getAllUsers()
                    Log.d(TAG, "Loaded ${users.size} users from database")
                    allUsers = users
                    // Initially, display all users
                    displayedUsers = users
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading users: ${e.message}", e)
                errorMessage = "Failed to load users: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    // Function to remove a user from the displayed list (not from the database)
    fun removeUserFromList(user: User) {
        if (user.userId == currentUserId) {
            Toast.makeText(context, "You cannot remove yourself from the list", Toast.LENGTH_LONG).show()
            return
        }

        // Filter user out of the displayed list
        displayedUsers = displayedUsers.filter { it.userId != user.userId }
        Toast.makeText(context, "User removed from list", Toast.LENGTH_SHORT).show()
    }

    // Load users when screen is first displayed
    LaunchedEffect(Unit) {
        loadUsers()
    }

    // Remove confirmation dialog
    userToRemove?.let { user ->
        AlertDialog(
            onDismissRequest = { userToRemove = null },
            title = { Text("Remove from List") },
            text = {
                Text("Are you sure you want to remove user '${user.username}' from the list? This will only hide them from this view.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        removeUserFromList(user)
                        userToRemove = null
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { userToRemove = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User List") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Reset button - shows all users again
                    if (displayedUsers.size < allUsers.size) {
                        TextButton(onClick = {
                            displayedUsers = allUsers
                            Toast.makeText(context, "Showing all users", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Show All")
                        }
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
                            text = "You need to login to view users",
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
                errorMessage != null -> {
                    // Error state
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadUsers() }) {
                            Text("Try Again")
                        }
                    }
                }
                displayedUsers.isEmpty() -> {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (allUsers.isEmpty()) {
                            // No users at all
                            Text(
                                text = "No Users Found",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navController.navigate(Screen.AddUserScreen.route) }) {
                                Text("Add User")
                            }
                        } else {
                            // Users exist but all have been removed from display
                            Text(
                                text = "All Users Removed",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You've removed all users from the list",
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                displayedUsers = allUsers
                            }) {
                                Text("Show All Users")
                            }
                        }
                    }
                }
                else -> {
                    // User list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(displayedUsers) { user ->
                            UserCard(
                                user = user,
                                currentUserId = currentUserId,
                                onRemove = {
                                    if (user.userId == currentUserId) {
                                        Toast.makeText(context, "You cannot remove yourself from the list", Toast.LENGTH_LONG).show()
                                    } else {
                                        userToRemove = user
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(
    user: User,
    currentUserId: String,
    onRemove: () -> Unit
) {
    val isCurrentUser = user.userId == currentUserId

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Username and status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.username ?: "Unknown",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (isCurrentUser) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "You",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${user.firstName ?: ""} ${user.lastName ?: ""}",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Phone
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = user.phone ?: "N/A",
                    fontSize = 14.sp
                )
            }

            // Only show remove button if not the current user
            if (!isCurrentUser) {
                Spacer(modifier = Modifier.height(12.dp))

                // Remove button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onRemove
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Remove from list",
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
}