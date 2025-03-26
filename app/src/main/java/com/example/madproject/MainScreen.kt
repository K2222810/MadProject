package com.example.madproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.madproject.sampledata.DatabaseInstance
import com.example.madproject.sampledata.UserSessionManager
import com.example.madproject.viewmodel.AlertViewModel
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController
) {
    var alertActive by remember { mutableStateOf<Boolean?>(null) }
    val isLoggedIn = UserSessionManager.isLoggedIn.value
    val username = UserSessionManager.username.value
    val currentUserId = UserSessionManager.userId.value
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isLoggedIn) "Welcome, $username" else "StaySafe App",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    if (isLoggedIn) {
                        IconButton(onClick = {
                            // Logout user and navigate back to login screen
                            UserSessionManager.endSession()
                            navController.navigate(Screen.LoginScreen.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ExitToApp,
                                contentDescription = "Logout"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddTripsScreen.route) },
                shape = CircleShape,
                containerColor = Color.White,
                contentColor = Color.Green,
                modifier = Modifier.size(72.dp)
            ) {
                Text("+", color = Color.Green, fontSize = 36.sp)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Ensures FAB does not overlap content
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(75.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { navController.navigate(Screen.MapScreen.route) },
                        modifier = Modifier.size(150.dp, 150.dp),
                        shape = RoundedCornerShape(30.dp)
                    ) {
                        Text(text = "GPS", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(50.dp))
                    Button(
                        onClick = {
                            if (isLoggedIn) {
                                alertActive = true
                            } else {
                                Toast.makeText(context, "Please login to send alerts", Toast.LENGTH_LONG).show()
                                navController.navigate(Screen.LoginScreen.route)
                            }
                        },
                        modifier = Modifier.size(150.dp, 150.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text(text = "!", fontSize = 72.sp)
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))

                // My Account Section
                Text(
                    text = "My Account",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Edit Profile button
                Button(
                    onClick = {
                        if (isLoggedIn) {
                            navController.navigate(Screen.EditUserScreen.createRoute(currentUserId))
                        } else {
                            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                            navController.navigate(Screen.LoginScreen.route)
                        }
                    },
                    modifier = Modifier.size(270.dp, 50.dp),
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
                    Text("Edit My Profile", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(30.dp))

                // User Management Section
                Text(
                    text = "User Management",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Add user button
                Button(
                    onClick = {
                        if (!isLoggedIn) {
                            navController.navigate(Screen.LoginScreen.route)
                        } else {
                            navController.navigate(Screen.AddUserScreen.route)
                        }
                    },
                    modifier = Modifier.size(270.dp, 50.dp)
                ) {
                    Text(text = "Add User", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))

                // View users button
                Button(
                    onClick = {
                        if (!isLoggedIn) {
                            navController.navigate(Screen.LoginScreen.route)
                        } else {
                            navController.navigate(Screen.UserListScreen.route)
                        }
                    },
                    modifier = Modifier.size(270.dp, 50.dp)
                ) {
                    Text(text = "View Users", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Trip Management Section
                Text(
                    text = "Trip Management",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!isLoggedIn) {
                            navController.navigate(Screen.LoginScreen.route)
                        } else {
                            navController.navigate(Screen.ViewMyTripsScreen.route)
                        }
                    },
                    modifier = Modifier.size(270.dp, 50.dp)
                ) {
                    Text(text = "View Trips", fontSize = 18.sp)
                }

                // Removed "View Map" button as requested

                // Notifications Button
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (!isLoggedIn) {
                            navController.navigate(Screen.LoginScreen.route)
                        } else {
                            navController.navigate(Screen.NotificationsScreen.route)
                        }
                    },
                    modifier = Modifier.size(270.dp, 50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(text = "Emergency Alerts", fontSize = 18.sp)
                }
            }
        }
    }

    alertActive?.let {
        CreateAlert(onDismiss = { alertActive = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlert(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val database = DatabaseInstance.getDatabase(context)
    val viewModelStoreOwner = LocalViewModelStoreOwner.current!!
    val alertViewModel: AlertViewModel = viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AlertViewModel(database) as T
            }
        }
    )

    var message by remember { mutableStateOf("") }
    val locationDescription by remember {
        mutableStateOf("Current location") // This could be enhanced with actual GPS coordinates
    }

    // Observe the alert status
    val alertSent by alertViewModel.alertSent.observeAsState(false)
    val error by alertViewModel.error.observeAsState("")

    // Handle successful alert sending
    LaunchedEffect(alertSent) {
        if (alertSent) {
            Toast.makeText(context, "Emergency alert sent to all contacts", Toast.LENGTH_LONG).show()
            onDismiss()
        }
    }

    // Handle errors
    LaunchedEffect(error) {
        if (error.isNotEmpty()) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Send Emergency Alert",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Alert Message (optional)") },
                    placeholder = { Text("I need help!") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Text(
                    "This will send an emergency alert to all your contacts",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            alertViewModel.sendEmergencyAlert(
                                message = if (message.isBlank()) "Emergency alert! I need help!" else message,
                                locationDescription = locationDescription
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Send Alert")
                    }
                }
            }
        }
    }
}