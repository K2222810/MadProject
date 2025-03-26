package com.example.madproject

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.madproject.sampledata.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ViewTripsScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewTripsScreen(
    navController: NavController,
    viewMyTrips: Boolean = true // Kept for compatibility
) {
    val context = LocalContext.current
    val database = DatabaseInstance.getDatabase(context)
    val scope = rememberCoroutineScope()

    // Get current user information
    val isLoggedIn = UserSessionManager.isLoggedIn.value
    val currentUserId = UserSessionManager.userId.value

    // If not logged in, redirect to login
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate(Screen.LoginScreen.route) {
                popUpTo(navController.graph.startDestinationId)
            }
        }
    }

    // State for trips and deletion confirmation
    var trips by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var tripToDelete by remember { mutableStateOf<Activity?>(null) }

    // Format for displaying dates
    val dateFormatter = remember { SimpleDateFormat("dd MMM, yyyy HH:mm", Locale.getDefault()) }

    // Function to refresh trips from database
    fun loadTrips() {
        if (!isLoggedIn) {
            errorMessage = "Please login to view your trips"
            isLoading = false
            return
        }

        isLoading = true
        errorMessage = null

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val allTrips = database.activityDao().getAllActivities()
                    Log.d(TAG, "Loaded ${allTrips.size} trips from database")

                    // Filter trips to only show this user's trips
                    trips = allTrips.filter { it.userId == currentUserId }

                    Log.d(TAG, "Filtered to ${trips.size} trips for user $currentUserId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading trips: ${e.message}", e)
                errorMessage = "Failed to load trips: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    // Function to delete a trip
    fun deleteTrip(trip: Activity) {
        scope.launch {
            try {
                // Only allow deletion of the user's own trips
                if (trip.userId != currentUserId) {
                    Toast.makeText(context, "You can only delete your own trips", Toast.LENGTH_LONG).show()
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    // Delete the activity
                    database.activityDao().deleteActivityById(trip.activityId)

                    // Optionally, also clean up related data (locations and status)
                    // This is a basic implementation - in a real app, you might want to check
                    // if these are used by other activities before deleting
                    database.locationDao().deleteLocationById(trip.fromLocationId)
                    database.locationDao().deleteLocationById(trip.toLocationId)
                    database.statusDao().deleteStatusById(trip.statusId)
                }

                // Show success message
                Toast.makeText(context, "Trip deleted successfully", Toast.LENGTH_SHORT).show()

                // Refresh the list
                loadTrips()

            } catch (e: Exception) {
                Log.e(TAG, "Error deleting trip: ${e.message}", e)
                Toast.makeText(context, "Error deleting trip: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Load trips when screen is first displayed
    LaunchedEffect(currentUserId) {
        loadTrips()
    }

    // Delete confirmation dialog
    tripToDelete?.let { trip ->
        AlertDialog(
            onDismissRequest = { tripToDelete = null },
            title = { Text("Delete Trip") },
            text = { Text("Are you sure you want to delete '${trip.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        deleteTrip(trip)
                        tripToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { tripToDelete = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Trips") },
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
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Please Login",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "You need to login to view your trips",
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { navController.navigate(Screen.LoginScreen.route) }) {
                            Text("Go to Login")
                        }
                    }
                }
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
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
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.popBackStack() }) {
                            Text("Go Back")
                        }
                    }
                }
                trips.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No trips found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You haven't created any trips yet",
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { navController.navigate(Screen.AddTripsScreen.route) }) {
                            Text("Add a Trip")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(trips) { trip ->
                            TripCard(
                                trip = trip,
                                dateFormatter = dateFormatter,
                                onClick = {
                                    // View trip details (for future expansion)
                                    Toast.makeText(context, "Viewing trip: ${trip.name}", Toast.LENGTH_SHORT).show()
                                },
                                onEdit = {
                                    // Navigate to edit screen with trip ID
                                    // Only allow editing user's own trips
                                    if (trip.userId == currentUserId) {
                                        navController.navigate(Screen.EditTripScreen.createRoute(trip.activityId))
                                    } else {
                                        Toast.makeText(context, "You can only edit your own trips", Toast.LENGTH_LONG).show()
                                    }
                                },
                                onDelete = {
                                    // Only allow deletion of user's own trips
                                    if (trip.userId == currentUserId) {
                                        tripToDelete = trip
                                    } else {
                                        Toast.makeText(context, "You can only delete your own trips", Toast.LENGTH_LONG).show()
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
fun TripCard(
    trip: Activity,
    dateFormatter: SimpleDateFormat,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Check if the trip belongs to current user
    val isUserOwnTrip = trip.userId == UserSessionManager.userId.value

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Trip name and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                val statusColor = when (trip.statusName) {
                    "Planned" -> Color(0xFF3F51B5)
                    "In Progress" -> Color(0xFF4CAF50)
                    "Completed" -> Color(0xFF607D8B)
                    "Cancelled" -> Color(0xFFF44336)
                    else -> Color(0xFF9E9E9E)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = trip.statusName,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trip description
            Text(
                text = trip.description,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Locations
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${trip.fromLocationName} → ${trip.toLocationName}",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dates
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${dateFormatter.format(Date(trip.leaveTime))} - ${dateFormatter.format(Date(trip.arriveTime))}",
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons - Only show for user's own trips
            if (isUserOwnTrip) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Edit button
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Delete button
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}