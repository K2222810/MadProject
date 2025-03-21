package com.example.madproject

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Calendar

@Composable
fun AddTripsScreen(navController: NavController) {
    val context = LocalContext.current
    var activityName by remember { mutableStateOf("") }
    var activityDescription by remember { mutableStateOf("") }
    var activityLeave by remember { mutableStateOf("") }
    var activityArrive by remember { mutableStateOf("") }
    var fromLocation by remember { mutableStateOf("") }
    var toLocation by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("") }

    val statuses = listOf("Planned", "In Progress", "Completed", "Cancelled", "Other")

    fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSelected("$year-${month + 1}-$dayOfMonth")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

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

            Button(onClick = { showDatePickerDialog { activityLeave = it } }, modifier = Modifier.fillMaxWidth()) {
                Text(if (activityLeave.isEmpty()) "Select Leave Time" else "Leave Time: $activityLeave")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { showDatePickerDialog { activityArrive = it } }, modifier = Modifier.fillMaxWidth()) {
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
                onClick = {
                    if (activityName.isNotBlank() && activityDescription.isNotBlank() && activityLeave.isNotBlank() && activityArrive.isNotBlank() && fromLocation.isNotBlank() && toLocation.isNotBlank() && selectedStatus.isNotBlank()) {
                        Toast.makeText(context, "Trip saved!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                },
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

@Composable
fun DropdownMenuField(selectedOption: String, options: List<String>, label: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
                }
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(
                    onClick = {
                        onOptionSelected(it)
                        expanded = false
                    },
                    text = { Text(it) }
                )
            }
        }
    }
}