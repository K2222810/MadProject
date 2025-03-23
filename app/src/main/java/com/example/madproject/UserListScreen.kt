package com.example.madproject
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.madproject.sampledata.viewmodel.UserViewModel

@Composable
fun UserListScreen(navController: NavController,
                   viewModel: UserViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val users = viewModel.users.observeAsState().value ?: emptyList()

    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "User List", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(users) { user ->
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Username: ${user.username}")
                        Text(text = "Name: ${user.firstName} ${user.lastName}")
                        Text(text = "Phone: ${user.phone ?: "N/A"}")
                    }
                }
            }
        }
    }
}