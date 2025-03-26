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
import androidx.compose.material.icons.filled.Search
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
import com.example.madproject.sampledata.Contact
import com.example.madproject.sampledata.DatabaseInstance
import com.example.madproject.sampledata.User
import com.example.madproject.sampledata.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "UserListScreen"

// Data class to hold contact information with user details
data class ContactWithUserDetails(
    val contactId: String,
    val userId: String,
    val contactUserId: String,
    val label: String,
    val dateCreated: Long,
    // User details
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val phone: String?
)

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

    // State for contacts
    var allContacts by remember { mutableStateOf<List<ContactWithUserDetails>>(emptyList()) }
    var displayedContacts by remember { mutableStateOf<List<ContactWithUserDetails>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var contactToDelete by remember { mutableStateOf<ContactWithUserDetails?>(null) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }

    // Function to filter contacts based on search query
    fun filterContacts(query: String) {
        if (query.isBlank()) {
            displayedContacts = allContacts
            return
        }

        val lowerCaseQuery = query.lowercase()
        displayedContacts = allContacts.filter { contact ->
            val firstName = contact.firstName?.lowercase() ?: ""
            val lastName = contact.lastName?.lowercase() ?: ""
            val fullName = "$firstName $lastName"
            val phone = contact.phone?.lowercase() ?: ""
            val username = contact.username?.lowercase() ?: ""
            val label = contact.label.lowercase()

            fullName.contains(lowerCaseQuery) ||
                    phone.contains(lowerCaseQuery) ||
                    username.contains(lowerCaseQuery) ||
                    label.contains(lowerCaseQuery)
        }
    }

    // Function to load contacts from database
    fun loadContacts() {
        isLoading = true
        errorMessage = null

        scope.launch {
            try {
                val contactsWithUserDetails = withContext(Dispatchers.IO) {
                    // Get all contacts for the current user
                    val contacts = database.contactDao().getAllContacts()
                        .filter { it.userId == currentUserId }

                    // Get all users to get their details
                    val users = database.userDao().getAllUsers()

                    // Combine contact data with user details
                    contacts.mapNotNull { contact ->
                        val user = users.find { it.userId == contact.contactUserId }
                        if (user != null) {
                            ContactWithUserDetails(
                                contactId = contact.contactId,
                                userId = contact.userId,
                                contactUserId = contact.contactUserId,
                                label = contact.label,
                                dateCreated = contact.dateCreated,
                                username = user.username,
                                firstName = user.firstName,
                                lastName = user.lastName,
                                phone = user.phone
                            )
                        } else null
                    }
                }

                Log.d(TAG, "Loaded ${contactsWithUserDetails.size} contacts for user $currentUserId")
                allContacts = contactsWithUserDetails

                // Apply current search filter
                if (searchQuery.isBlank()) {
                    displayedContacts = contactsWithUserDetails
                } else {
                    filterContacts(searchQuery)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading contacts: ${e.message}", e)
                errorMessage = "Failed to load contacts: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    // Function to delete a contact
    fun deleteContact(contact: ContactWithUserDetails) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    database.contactDao().deleteContactById(contact.contactId)
                }

                // Remove from displayed and all contacts lists
                displayedContacts = displayedContacts.filter { it.contactId != contact.contactId }
                allContacts = allContacts.filter { it.contactId != contact.contactId }

                Toast.makeText(context, "Contact removed successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting contact: ${e.message}", e)
                Toast.makeText(context, "Error removing contact: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Load contacts when screen is first displayed
    LaunchedEffect(Unit) {
        loadContacts()
    }

    // Delete confirmation dialog
    contactToDelete?.let { contact ->
        AlertDialog(
            onDismissRequest = { contactToDelete = null },
            title = { Text("Remove Contact") },
            text = {
                Text("Are you sure you want to remove ${contact.username ?: "this contact"} from your contacts?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteContact(contact)
                        contactToDelete = null
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { contactToDelete = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Contacts") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Profile button
                    IconButton(
                        onClick = {
                            if (isLoggedIn) {
                                // Navigate to edit user screen with current user ID
                                navController.navigate(Screen.EditUserScreen.createRoute(currentUserId))
                            } else {
                                Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
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
                            text = "You need to login to view your contacts",
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
                        Button(onClick = { loadContacts() }) {
                            Text("Try Again")
                        }
                    }
                }
                else -> {
                    // Main content with search and contact list
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Search bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                filterContacts(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            placeholder = { Text("Search contacts") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        displayedContacts = allContacts
                                    }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear search"
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        )

                        // Display search results count if searching
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "Found ${displayedContacts.size} results",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (displayedContacts.isEmpty()) {
                            // Empty contacts state
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (searchQuery.isNotEmpty()) {
                                    // No search results
                                    Text(
                                        text = "No contacts found matching '$searchQuery'",
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = {
                                        searchQuery = ""
                                        displayedContacts = allContacts
                                    }) {
                                        Text("Clear Search")
                                    }
                                } else {
                                    // No contacts at all
                                    Text(
                                        text = "No Contacts Found",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "You haven't added any contacts yet",
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        } else {
                            // Contact list
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(displayedContacts) { contact ->
                                    ContactCard(
                                        contact = contact,
                                        onRemove = {
                                            contactToDelete = contact
                                        }
                                    )
                                }
                            }
                        }

                        // Row with buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Add user button
                            Button(
                                onClick = { navController.navigate(Screen.AddUserScreen.route) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Add Contact")
                            }

                            // Friend requests button
                            Button(
                                onClick = { navController.navigate(Screen.FriendRequestsScreen.route) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Friend Requests")
                            }
                        }

                        // My Profile button
                        Button(
                            onClick = {
                                if (isLoggedIn) {
                                    navController.navigate(Screen.EditUserScreen.createRoute(currentUserId))
                                } else {
                                    Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "My Profile",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit My Profile")
                        }

                        // Maps button
                        Button(
                            onClick = {
                                try {
                                    // Navigate to the map screen
                                    navController.navigate(Screen.MapScreen.route)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error navigating to map: ${e.message}", e)
                                    Toast.makeText(context, "Could not open map: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("View Map")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactCard(
    contact: ContactWithUserDetails,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Username and label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = contact.username ?: "Unknown",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (contact.label.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = contact.label,
                            color = MaterialTheme.colorScheme.primary,
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
                    text = "${contact.firstName ?: ""} ${contact.lastName ?: ""}",
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
                    text = contact.phone ?: "N/A",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Remove button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Remove contact",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}