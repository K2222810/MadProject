package com.example.madproject

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madproject.sampledata.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.text.SimpleDateFormat

// Define a tag for logging
private const val TAG = "EditTripScreen"

@Composable
fun EditTripScreen(
    navController: NavController,
    tripId: String
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
            Toast.makeText(context, "Please login to edit trips", Toast.LENGTH_LONG).show()
            navController.navigate(Screen.LoginScreen.route) {
                popUpTo(navController.graph.startDestinationId)
            }
        }
    }

    // Trip details
    var activityName by remember { mutableStateOf("") }
    var activityDescription by remember { mutableStateOf("") }
    var activityLeave by remember { mutableStateOf("") }
    var activityArrive by remember { mutableStateOf("") }
    var fromLocation by remember { mutableStateOf("") }
    var toLocation by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("") }

    // Original trip owner
    var tripUserId by remember { mutableStateOf("") }

    // IDs of related entities to update
    var fromLocationId by remember { mutableStateOf("") }
    var toLocationId by remember { mutableStateOf("") }
    var statusId by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    // Store date objects separately for proper database timestamp conversion
    var leaveDate by remember { mutableStateOf<Calendar?>(null) }
    var arriveDate by remember { mutableStateOf<Calendar?>(null) }

    // Loading and authorization states
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var isAuthorized by remember { mutableStateOf(true) } // Assume authorized until we check

    val statuses = listOf("Planned", "In Progress", "Completed", "Cancelled", "Other")
    val dateFormatter = SimpleDateFormat("dd MMM, yyyy HH:mm", Locale.getDefault())

    // Load trip data
    LaunchedEffect(tripId, currentUserId) {
        withContext(Dispatchers.IO) {
            try {
                // Fetch trip data
                val activities = database.activityDao().getAllActivities()
                val trip = activities.find { it.activityId == tripId }

                if (trip != null) {
                    // Check if user is authorized to edit this trip
                    tripUserId = trip.userId
                    isAuthorized = trip.userId == currentUserId

                    if (!isAuthorized) {
                        loadError = "You are not authorized to edit this trip"
                        Log.e(TAG, "Unauthorized edit attempt: User $currentUserId trying to edit trip owned by ${trip.userId}")
                        isLoading = false
                        return@withContext
                    }

                    // Update state with trip data
                    activityName = trip.name
                    activityDescription = trip.description
                    fromLocation = trip.fromLocationName
                    toLocation = trip.toLocationName
                    selectedStatus = trip.statusName

                    // Store IDs for updating
                    fromLocationId = trip.fromLocationId
                    toLocationId = trip.toLocationId
                    statusId = trip.statusId
                    username = trip.username

                    // Set up dates
                    val leaveCalendar = Calendar.getInstance()
                    leaveCalendar.timeInMillis = trip.leaveTime
                    leaveDate = leaveCalendar

                    val arriveCalendar = Calendar.getInstance()
                    arriveCalendar.timeInMillis = trip.arriveTime
                    arriveDate = arriveCalendar

                    // Format dates for display
                    activityLeave = dateFormatter.format(Date(trip.leaveTime))
                    activityArrive = dateFormatter.format(Date(trip.arriveTime))

                    Log.d(TAG, "Loaded trip data: $activityName")
                } else {
                    loadError = "Trip not found"
                    Log.e(TAG, "Trip not found with ID: $tripId")
                }
            } catch (e: Exception) {
                loadError = "Error loading trip: ${e.localizedMessage}"
                Log.e(TAG, "Error loading trip: ${e.message}", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun showDateTimePickerDialog(isLeaveTime: Boolean) {
        val calendar = Calendar.getInstance()

        // First, show the DatePickerDialog
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                // After date is selected, show TimePickerDialog
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val selectedCalendar = Calendar.getInstance()
                        selectedCalendar.set(year, month, dayOfMonth, hourOfDay, minute)

                        val formattedDate = "$year-${month + 1}-$dayOfMonth"
                        val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                        val formattedDateTime = "$formattedDate $formattedTime"

                        if (isLeaveTime) {
                            activityLeave = formattedDateTime
                            leaveDate = selectedCalendar
                            Log.d(TAG, "Leave date and time set: $formattedDateTime, timestamp: ${selectedCalendar.timeInMillis}")
                        } else {
                            activityArrive = formattedDateTime
                            arriveDate = selectedCalendar
                            Log.d(TAG, "Arrive date and time set: $formattedDateTime, timestamp: ${selectedCalendar.timeInMillis}")
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true // Use 24-hour format
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Function to update trip in database
    fun updateTrip() {
        Log.d(TAG, "Attempting to update trip")

        // Check if user is logged in and authorized
        if (!isLoggedIn) {
            Toast.makeText(context, "Please login to edit trips", Toast.LENGTH_LONG).show()
            navController.navigate(Screen.LoginScreen.route)
            return
        }

        if (tripUserId != currentUserId) {
            Toast.makeText(context, "You are not authorized to edit this trip", Toast.LENGTH_LONG).show()
            navController.popBackStack()
            return
        }

        // Validate inputs
        if (activityName.isBlank() || activityDescription.isBlank() ||
            leaveDate == null || arriveDate == null ||
            fromLocation.isBlank() || toLocation.isBlank() ||
            selectedStatus.isBlank()) {
            Log.w(TAG, "Validation failed - missing fields")
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Validation passed, updating in database...")

        scope.launch {
            try {
                // Create and update from location
                val fromLoc = Location(
                    locationId = fromLocationId,
                    name = fromLocation,
                    description = "Updated from EditTripScreen",
                    address = "",
                    postcode = "",
                    latitude = 0.0,
                    longitude = 0.0
                )

                // Create and update to location
                val toLoc = Location(
                    locationId = toLocationId,
                    name = toLocation,
                    description = "Updated from EditTripScreen",
                    address = "",
                    postcode = "",
                    latitude = 0.0,
                    longitude = 0.0
                )

                // Create and update status
                val status = Status(
                    statusId = statusId,
                    name = selectedStatus,
                    order = statuses.indexOf(selectedStatus)
                )

                // Create and update activity
                val activity = Activity(
                    activityId = tripId,
                    name = activityName,
                    userId = currentUserId, // Ensure we preserve current user ID
                    username = UserSessionManager.username.value, // Use current username
                    description = activityDescription,
                    fromLocationId = fromLocationId,
                    fromLocationName = fromLocation,
                    leaveTime = leaveDate?.timeInMillis ?: System.currentTimeMillis(),
                    toLocationId = toLocationId,
                    toLocationName = toLocation,
                    arriveTime = arriveDate?.timeInMillis ?: System.currentTimeMillis(),
                    statusId = statusId,
                    statusName = selectedStatus
                )

                Log.d(TAG, "Created all entities, now updating database")

                // Update everything in database
                var updatedSuccessfully = false

                try {
                    withContext(Dispatchers.IO) {
                        // Update records in database
                        database.locationDao().insertLocation(fromLoc) // Room's insert handles updates with same ID
                        database.locationDao().insertLocation(toLoc)
                        database.statusDao().insertStatus(status)
                        database.activityDao().insertActivity(activity)

                        // Verify update
                        val updatedActivity = database.activityDao().getAllActivities()
                            .find { it.activityId == tripId }

                        updatedSuccessfully = updatedActivity?.name == activityName
                    }

                    if (updatedSuccessfully) {
                        Log.d(TAG, "Database update successful")
                        Toast.makeText(context, "Trip updated successfully!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Log.e(TAG, "Data appeared to update but verification failed")
                        Toast.makeText(context, "Error: Failed to verify update", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Database update error: ${e.message}", e)
                    Toast.makeText(context, "Error updating trip: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "General error in updateTrip: ${e.message}", e)
                Toast.makeText(context, "Unexpected error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!isLoggedIn) {
            // Not logged in
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Login Required",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "You need to be logged in to edit trips",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Button(onClick = { navController.navigate(Screen.LoginScreen.route) }) {
                    Text("Go to Login")
                }
            }
        } else if (isLoading) {
            // Loading state
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.material3.CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading trip data...")
            }
        } else if (loadError != null || !isAuthorized) {
            // Error or unauthorized
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (!isAuthorized) "Not Authorized" else "Error",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = loadError ?: "You cannot edit this trip",
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Go Back")
                }
            }
        } else {
            // Regular edit form
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Edit Trip",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = activityName,
                    onValueChange = { activityName = it },
                    label = { Text("Activity Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = activityDescription,
                    onValueChange = { activityDescription = it },
                    label = { Text("Activity Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { showDateTimePickerDialog(true) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (activityLeave.isEmpty()) "Select Leave Time" else "Leave Time: $activityLeave")
                }
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { showDateTimePickerDialog(false) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (activityArrive.isEmpty()) "Select Arrive Time" else "Arrive Time: $activityArrive")
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fromLocation,
                    onValueChange = { fromLocation = it },
                    label = { Text("From Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = toLocation,
                    onValueChange = { toLocation = it },
                    label = { Text("To Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Dropdown for Activity Status
                DropdownMenuField(selectedOption = selectedStatus, options = statuses, label = "Activity Status") {
                    selectedStatus = it
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { updateTrip() },
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("Update Trip", fontSize = 18.sp)
                    }

                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("Cancel", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}