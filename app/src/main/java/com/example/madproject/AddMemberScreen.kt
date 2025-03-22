package com.example.madproject

import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Locale

@Composable
fun AddMemberScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var contactLabel by remember { mutableStateOf("") }

    val currentDate: String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Calendar.getInstance().time)

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = contactLabel,
                onValueChange = { contactLabel = it },
                label = { Text("Contact Label") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val newMember = mapOf(
                        "UserUsername" to username,
                        "UserContactLabel" to contactLabel,
                        "UserContactDatecreated" to currentDate
                    )
                    // Handle saving newMember data (e.g., sending to a database or API call)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Add Member", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { navController.navigate(Screen.MainScreen.route) },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Back", fontSize = 16.sp)
            }
        }
    }
}