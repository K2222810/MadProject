package com.example.madproject

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madproject.sampledata.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

private const val TAG = "AddUserScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val database = DatabaseInstance.getDatabase(context)
    val scope = rememberCoroutineScope()

    // Check if current user is logged in
    val isLoggedIn = UserSessionManager.isLoggedIn.value
    val currentUserId = UserSessionManager.userId.value
    val currentUsername = UserSessionManager.username.value

    // State for user input
    var searchUsername by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<User?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var requestMessage by remember { mutableStateOf("") }

    // Function to search for a user by username
    fun searchUser() {
        if (searchUsername.isBlank()) {
            searchError = "Please enter a username to search"
            return
        }

        isSearching = true
        searchResult = null
        searchError = null

        scope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    database.userDao().getUserByUsername(searchUsername)
                }

                if (user != null) {
                    if (user.userId == currentUserId) {
                        searchError = "You cannot add yourself as a contact"
                    } else {
                        // Check if already a contact or if there's a pending request
                        val contacts = withContext(Dispatchers.IO) {
                            database.contactDao().getAllContacts()
                        }

                        val isAlreadyContact = contacts.any {
                            it.userId == currentUserId && it.contactUserId == user.userId
                        }

                        if (isAlreadyContact) {
                            searchError = "This user is already in your contacts"
                            return@launch
                        }

                        // Check for existing friend requests
                        val existingRequests = withContext(Dispatchers.IO) {
                            database.friendRequestDao().getRequestsBetweenUsers(currentUserId, user.userId)
                        }

                        if (existingRequests.isNotEmpty()) {
                            val pendingRequest = existingRequests.find { it.status == "PENDING" }
                            if (pendingRequest != null) {
                                if (pendingRequest.senderId == currentUserId) {
                                    searchError = "You already sent a friend request to this user"
                                } else {
                                    searchError = "This user already sent you a friend request. Check your friend requests."
                                }
                                return@launch
                            }
                        }

                        searchResult = user
                    }
                } else {
                    searchError = "User not found"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error searching for user: ${e.message}", e)
                searchError = "Error searching for user: ${e.localizedMessage}"
            } finally {
                isSearching = false
            }
        }
    }

    // Function to send friend request
    fun sendFriendRequest() {
        if (searchResult == null) {
            return
        }

        scope.launch {
            try {
                val newRequest = FriendRequest(
                    requestId = UUID.randomUUID().toString(),
                    senderId = currentUserId,
                    senderUsername = currentUsername,
                    receiverId = searchResult!!.userId,
                    status = "PENDING",
                    message = requestMessage.ifBlank { "I'd like to add you as a contact" },
                    timestamp = System.currentTimeMillis()
                )

                withContext(Dispatchers.IO) {
                    database.friendRequestDao().insertFriendRequest(newRequest)
                }

                Toast.makeText(context, "Friend request sent", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            } catch (e: Exception) {
                Log.e(TAG, "Error sending friend request: ${e.message}", e)
                Toast.makeText(context, "Error sending friend request: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Contact") },
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
            if (!isLoggedIn) {
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
                        text = "You need to login to add contacts",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { navController.navigate(Screen.LoginScreen.route) }) {
                        Text("Go to Login")
                    }
                }
            } else {
                // Add user form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Search for Contacts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Search bar
                    OutlinedTextField(
                        value = searchUsername,
                        onValueChange = { searchUsername = it },
                        label = { Text("Enter Username") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        trailingIcon = {
                            IconButton(onClick = { searchUser() }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { searchUser() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Search User")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Show search results or error
                    when {
                        isSearching -> {
                            CircularProgressIndicator()
                        }
                        searchError != null -> {
                            Text(
                                text = searchError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        searchResult != null -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "User Found:",
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text("Username: ${searchResult?.username ?: ""}")
                                    Text("Name: ${searchResult?.firstName ?: ""} ${searchResult?.lastName ?: ""}")

                                    Spacer(modifier = Modifier.height(16.dp))

                                    OutlinedTextField(
                                        value = requestMessage,
                                        onValueChange = { requestMessage = it },
                                        label = { Text("Friend Request Message (optional)") },
                                        placeholder = { Text("I'd like to add you as a contact") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = { sendFriendRequest() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Send Friend Request")
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Link to view friend requests
                    TextButton(
                        onClick = { navController.navigate(Screen.FriendRequestsScreen.route) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View My Friend Requests")
                    }
                }
            }
        }
    }
}