package com.example.madproject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
                Button(onClick = {}, modifier = Modifier.size(150.dp, 150.dp), shape = RoundedCornerShape(30.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text(text = "!", fontSize = 72.sp)
                }
        }
        Spacer(modifier = Modifier.height(100.dp))
        Button(onClick = {}, modifier = Modifier.size(270.dp, 50.dp)) {
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