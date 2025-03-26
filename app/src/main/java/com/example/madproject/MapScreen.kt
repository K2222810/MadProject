package com.example.madproject

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current // Get context
    val intent = Intent(context, MapsMarker::class.java)
    context.startActivity(intent)
}