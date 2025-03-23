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
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun MainScreen(
    navController: NavController
){
    var alertActive by remember { mutableStateOf<Boolean?>(null) }
    Scaffold(
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
    ){ paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Ensures FAB does not overlap content
        ){
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(75.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center) {
                    Button(onClick = {}, modifier = Modifier.size(150.dp, 150.dp), shape = RoundedCornerShape(30.dp)) {
                        Text(text = "GPS", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(50.dp))
                    Button(onClick = {alertActive=true}, modifier = Modifier.size(150.dp, 150.dp), shape = RoundedCornerShape(30.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                        Text(text = "!", fontSize = 72.sp)
                    }
                }
                Spacer(modifier = Modifier.height(100.dp))
<<<<<<< Updated upstream
                Button(onClick = {}, modifier = Modifier.size(270.dp, 50.dp)) {
=======
                Button(onClick = { navController.navigate(Screen.UserListScreen.route) }, modifier = Modifier.size(270.dp, 50.dp)) {
>>>>>>> Stashed changes
                    Text(text = "Add members", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = {}, modifier = Modifier.size(270.dp, 50.dp)) {
                    Text(text = "View my trips", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = {}, modifier = Modifier.size(270.dp, 50.dp)) {
                    Text(text = "View other trips", fontSize = 24.sp)
                }
            }
        }
        }

    alertActive?.let {
        CreateDialog(
            onDismiss= { alertActive = null }
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDialog(onDismiss: () -> Unit) {
    BasicAlertDialog(onDismissRequest = onDismiss){
        Surface(
            modifier = Modifier.wrapContentWidth().wrapContentHeight().padding(16.dp),
            shape = MaterialTheme. shapes. large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ){
            Column(modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                ){
                Text("Are you sure you want to send an alert?")
                Row(modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center){
                    Button(onClick = onDismiss){ //Add additional functionality to the confirm button (send an alert)
                        Text("Confirm")
                    }
                    Spacer(modifier = Modifier.width(30.dp))
                    Button(onClick = onDismiss){
                        Text("Cancel")
                    }
                }
            }
        }
    }
}