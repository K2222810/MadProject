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
import com.example.madproject.sampledata.Contact
import com.example.madproject.sampledata.DatabaseInstance
import com.example.madproject.sampledata.User
import com.example.madproject.sampledata.UserSessionManager
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

    // State for user input
    var searchUsername by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<User?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var contactLabel by remember { mutableStateOf("") }

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
                        // Check if already a contact
                        val contacts = withContext(Dispatchers.IO) {
                            database.contactDao().getAllContacts()
                        }

                        val isAlreadyContact = contacts.any {
                            it.userId == currentUserId && it.contactUserId == user.userId
                        }

                        if (isAlreadyContact) {
                            searchError = "This user is already in your contacts"
                        } else {
                            searchResult = user
                        }
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

    // Function to add user to contacts
    fun addUserToContacts() {
        if (searchResult == null) {
            return
        }

        scope.launch {
            try {
                val newContact = Contact(
                    contactId = UUID.randomUUID().toString(),
                    userId = currentUserId,
                    contactUserId = searchResult!!.userId,
                    label = contactLabel.ifBlank { "Contact" },
                    dateCreated = System.currentTimeMillis()
                )

                withContext(Dispatchers.IO) {
                    database.contactDao().insertContact(newContact)
                }

                Toast.makeText(context, "User added to contacts", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            } catch (e: Exception) {
                Log.e(TAG, "Error adding contact: ${e.message}", e)
                Toast.makeText(context, "Error adding contact: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Existing User") },
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
                        text = "You need to login to add users to your contacts",
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
                        text = "Add Existing User to Contacts",
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
                                        value = contactLabel,
                                        onValueChange = { contactLabel = it },
                                        label = { Text("Contact Label (optional)") },
                                        placeholder = { Text("Friend, Colleague, etc.") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = { addUserToContacts() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Add to Contacts")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}