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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.madproject.sampledata.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

// Define a tag for logging
private const val TAG = "AddTripsScreen"

@Composable
fun AddTripsScreen(navController: NavController) {
    val context = LocalContext.current
    val database = DatabaseInstance.getDatabase(context)
    val scope = rememberCoroutineScope()

    // Get current user information
    val isLoggedIn = UserSessionManager.isLoggedIn.value
    val currentUserId = UserSessionManager.userId.value
    val currentUsername = UserSessionManager.username.value

    // If not logged in, redirect to login
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            Toast.makeText(context, "Please login to add trips", Toast.LENGTH_LONG).show()
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

    // Store date objects separately for proper database timestamp conversion
    var leaveDate by remember { mutableStateOf<Calendar?>(null) }
    var arriveDate by remember { mutableStateOf<Calendar?>(null) }

    val statuses = listOf("Planned", "In Progress", "Completed", "Cancelled", "Other")

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

    // Function to save trip to database
    fun saveTrip() {
        Log.d(TAG, "Attempting to save trip")

        // Check if user is logged in
        if (!isLoggedIn) {
            Toast.makeText(context, "Please login to save trips", Toast.LENGTH_LONG).show()
            navController.navigate(Screen.LoginScreen.route)
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

        Log.d(TAG, "Validation passed, saving to database...")

        scope.launch {
            try {
                // Generate unique IDs
                val activityId = UUID.randomUUID().toString()
                val fromLocationId = UUID.randomUUID().toString()
                val toLocationId = UUID.randomUUID().toString()
                val statusId = UUID.randomUUID().toString()

                Log.d(TAG, "Generated IDs: Activity=$activityId, FromLoc=$fromLocationId, ToLoc=$toLocationId, Status=$statusId")

                // Create and save from location
                val fromLoc = Location(
                    locationId = fromLocationId,
                    name = fromLocation,
                    description = "Created from AddTripsScreen",
                    address = "",  // Default values since we don't collect these
                    postcode = "",
                    latitude = 0.0,
                    longitude = 0.0
                )

                // Create and save to location
                val toLoc = Location(
                    locationId = toLocationId,
                    name = toLocation,
                    description = "Created from AddTripsScreen",
                    address = "",
                    postcode = "",
                    latitude = 0.0,
                    longitude = 0.0
                )

                // Create and save status
                val status = Status(
                    statusId = statusId,
                    name = selectedStatus,
                    order = statuses.indexOf(selectedStatus)
                )

                // Create and save activity - using current user's ID and username
                val activity = Activity(
                    activityId = activityId,
                    name = activityName,
                    userId = currentUserId,
                    username = currentUsername,
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

                Log.d(TAG, "Created all entities, now saving to database")

                // Save everything to database with detailed error logging
                var savedSuccessfully = false

                try {
                    withContext(Dispatchers.IO) {
                        Log.d(TAG, "Saving location 1: $fromLocation")
                        database.locationDao().insertLocation(fromLoc)

                        Log.d(TAG, "Saving location 2: $toLocation")
                        database.locationDao().insertLocation(toLoc)

                        Log.d(TAG, "Saving status: $selectedStatus")
                        database.statusDao().insertStatus(status)

                        Log.d(TAG, "Saving activity: $activityName")
                        database.activityDao().insertActivity(activity)

                        // Verify data was saved
                        val locations = database.locationDao().getAllLocations()
                        val statuses = database.statusDao().getAllStatuses()
                        val activities = database.activityDao().getAllActivities()

                        Log.d(TAG, "After save: Locations=${locations.size}, Statuses=${statuses.size}, Activities=${activities.size}")
                        savedSuccessfully = activities.any { it.activityId == activityId }
                    }

                    if (savedSuccessfully) {
                        Log.d(TAG, "Database save successful, activity verified in database")

                        // Send notification to contacts directly
                        try {
                            val alert = Alert(
                                alertId = UUID.randomUUID().toString(),
                                userId = currentUserId,
                                username = currentUsername,
                                message = "$currentUsername has created a new trip: $activityName",
                                location = "Trip notification",
                                timestamp = System.currentTimeMillis(),
                                isActive = true
                            )

                            withContext(Dispatchers.IO) {
                                database.alertDao().insertAlert(alert)
                            }

                            Log.d(TAG, "Trip notification sent successfully")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error sending trip notification: ${e.message}", e)
                        }

                        Toast.makeText(context, "Trip saved successfully!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Log.e(TAG, "Data appeared to save but activity not found in database")
                        Toast.makeText(context, "Error: Data not saved to database", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Database save error: ${e.message}", e)
                    Toast.makeText(context, "Error saving trip: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "General error in saveTrip: ${e.message}", e)
                Toast.makeText(context, "Unexpected error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // If not logged in, show login reminder
    if (!isLoggedIn) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Login Required",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "You need to be logged in to add trips",
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Button(
                    onClick = { navController.navigate(Screen.LoginScreen.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Go to Login")
                }
            }
        }
    } else {
        // Regular add trip screen
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
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

                Button(
                    onClick = { saveTrip() },
                    modifier = Modifier.padding(8.dp).fillMaxWidth().height(50.dp)
                ) {
                    Text("Save Trip", fontSize = 18.sp)
                }

                Button(
                    onClick = { navController.navigate(Screen.MainScreen.route) },
                    modifier = Modifier.padding(8.dp).fillMaxWidth().height(50.dp)
                ) {
                    Text("Back", fontSize = 18.sp)
                }
            }
        }
    }
}