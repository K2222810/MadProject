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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madproject.sampledata.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "FriendRequestsScreen"

data class FriendRequestWithDetails(
    val request: FriendRequest,
    val senderDetails: User? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val database = DatabaseInstance.getDatabase(context)
    val scope = rememberCoroutineScope()

    // Get current user information
    val isLoggedIn = UserSessionManager.isLoggedIn.value
    val currentUserId = UserSessionManager.userId.value

    // State for friend requests
    var pendingRequests by remember { mutableStateOf<List<FriendRequestWithDetails>>(emptyList()) }
    var sentRequests by remember { mutableStateOf<List<FriendRequestWithDetails>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Tab selection state
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Incoming Requests", "Sent Requests")

    // Date formatter for displaying timestamps
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    // Function to load friend requests
    fun loadFriendRequests() {
        if (!isLoggedIn) {
            return
        }

        isLoading = true
        errorMessage = null

        scope.launch {
            try {
                // Get all pending requests for this user
                val incomingRequests = withContext(Dispatchers.IO) {
                    database.friendRequestDao().getPendingRequestsForUser(currentUserId)
                }

                // Get all sent requests by this user
                val outgoingRequests = withContext(Dispatchers.IO) {
                    database.friendRequestDao().getSentRequestsForUser(currentUserId)
                }

                // Get all users for populating details
                val allUsers = withContext(Dispatchers.IO) {
                    database.userDao().getAllUsers()
                }

                // Combine requests with user details
                pendingRequests = incomingRequests.map { request ->
                    val sender = allUsers.find { it.userId == request.senderId }
                    FriendRequestWithDetails(request, sender)
                }

                sentRequests = outgoingRequests.map { request ->
                    val receiver = allUsers.find { it.userId == request.receiverId }
                    FriendRequestWithDetails(request, receiver)
                }

                Log.d(TAG, "Loaded ${pendingRequests.size} pending requests and ${sentRequests.size} sent requests")

            } catch (e: Exception) {
                Log.e(TAG, "Error loading friend requests: ${e.message}", e)
                errorMessage = "Error loading friend requests: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    // Function to accept a friend request
    fun acceptFriendRequest(request: FriendRequest) {
        scope.launch {
            try {
                // Update request status to ACCEPTED
                withContext(Dispatchers.IO) {
                    database.friendRequestDao().updateRequestStatus(request.requestId, "ACCEPTED")
                }

                // Create contact entries for both users
                val contact1 = Contact(
                    contactId = UUID.randomUUID().toString(),
                    userId = currentUserId,
                    contactUserId = request.senderId,
                    label = "Contact",
                    dateCreated = System.currentTimeMillis()
                )

                val contact2 = Contact(
                    contactId = UUID.randomUUID().toString(),
                    userId = request.senderId,
                    contactUserId = currentUserId,
                    label = "Contact",
                    dateCreated = System.currentTimeMillis()
                )

                withContext(Dispatchers.IO) {
                    database.contactDao().insertContact(contact1)
                    database.contactDao().insertContact(contact2)
                }

                Toast.makeText(context, "Friend request accepted", Toast.LENGTH_SHORT).show()

                // Reload requests
                loadFriendRequests()

            } catch (e: Exception) {
                Log.e(TAG, "Error accepting friend request: ${e.message}", e)
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Function to reject a friend request
    fun rejectFriendRequest(request: FriendRequest) {
        scope.launch {
            try {
                // Update request status to REJECTED
                withContext(Dispatchers.IO) {
                    database.friendRequestDao().updateRequestStatus(request.requestId, "REJECTED")
                }

                Toast.makeText(context, "Friend request rejected", Toast.LENGTH_SHORT).show()

                // Reload requests
                loadFriendRequests()

            } catch (e: Exception) {
                Log.e(TAG, "Error rejecting friend request: ${e.message}", e)
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Function to cancel a sent request
    fun cancelFriendRequest(request: FriendRequest) {
        scope.launch {
            try {
                // Delete the request
                withContext(Dispatchers.IO) {
                    database.friendRequestDao().deleteFriendRequest(request.requestId)
                }

                Toast.makeText(context, "Friend request canceled", Toast.LENGTH_SHORT).show()

                // Reload requests
                loadFriendRequests()

            } catch (e: Exception) {
                Log.e(TAG, "Error canceling friend request: ${e.message}", e)
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Load requests when screen is first displayed
    LaunchedEffect(Unit) {
        loadFriendRequests()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friend Requests") },
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
                        text = "You need to login to view friend requests",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { navController.navigate(Screen.LoginScreen.route) }) {
                        Text("Go to Login")
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Tabs for switching between incoming and sent requests
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        errorMessage != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = errorMessage ?: "",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        else -> {
                            // Display requests based on selected tab
                            when (selectedTabIndex) {
                                0 -> {
                                    // Incoming requests
                                    if (pendingRequests.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No pending friend requests",
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(pendingRequests) { requestWithDetails ->
                                                IncomingRequestCard(
                                                    requestWithDetails = requestWithDetails,
                                                    dateFormatter = dateFormatter,
                                                    onAccept = { acceptFriendRequest(requestWithDetails.request) },
                                                    onReject = { rejectFriendRequest(requestWithDetails.request) }
                                                )
                                            }
                                        }
                                    }
                                }
                                1 -> {
                                    // Sent requests
                                    if (sentRequests.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "You haven't sent any friend requests",
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            items(sentRequests) { requestWithDetails ->
                                                SentRequestCard(
                                                    requestWithDetails = requestWithDetails,
                                                    dateFormatter = dateFormatter,
                                                    onCancel = { cancelFriendRequest(requestWithDetails.request) }
                                                )
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
    }
}

@Composable
fun IncomingRequestCard(
    requestWithDetails: FriendRequestWithDetails,
    dateFormatter: SimpleDateFormat,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val request = requestWithDetails.request
    val sender = requestWithDetails.senderDetails

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User icon
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                // User info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = sender?.username ?: request.senderUsername,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    if (sender != null) {
                        Text(
                            text = "${sender.firstName ?: ""} ${sender.lastName ?: ""}",
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Sent: ${dateFormatter.format(Date(request.timestamp))}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Request message
            if (request.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = request.message,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Reject"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accept"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept")
                }
            }
        }
    }
}

@Composable
fun SentRequestCard(
    requestWithDetails: FriendRequestWithDetails,
    dateFormatter: SimpleDateFormat,
    onCancel: () -> Unit
) {
    val request = requestWithDetails.request
    val receiver = requestWithDetails.senderDetails // Note: This is actually the receiver for sent requests

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User icon
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                // User info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "To: ${receiver?.username ?: request.receiverId}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Status: ${request.status}",
                        fontSize = 14.sp,
                        color = when (request.status) {
                            "PENDING" -> Color(0xFFFFA000)
                            "ACCEPTED" -> Color(0xFF00C853)
                            "REJECTED" -> Color(0xFFD50000)
                            else -> Color.Gray
                        }
                    )

                    Text(
                        text = "Sent: ${dateFormatter.format(Date(request.timestamp))}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Request message
            if (request.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = request.message,
                        fontSize = 14.sp
                    )
                }
            }

            // Cancel button for pending requests
            if (request.status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Cancel"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel Request")
                    }
                }
            }
        }
    }
}