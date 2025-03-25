package com.example.madproject

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun MapScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val intent = Intent(context, MapsMarker::class.java)
    context.startActivity(intent)
}