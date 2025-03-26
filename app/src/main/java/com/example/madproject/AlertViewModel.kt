package com.example.madproject.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madproject.sampledata.Alert
import com.example.madproject.sampledata.AppDatabase
import com.example.madproject.sampledata.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AlertViewModel(private val database: AppDatabase) : ViewModel() {

    private val _alertSent = MutableLiveData<Boolean>()
    val alertSent: LiveData<Boolean> = _alertSent

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun sendEmergencyAlert(message: String, locationDescription: String) {
        if (!UserSessionManager.isLoggedIn.value) {
            _error.value = "You must be logged in to send alerts"
            return
        }

        viewModelScope.launch {
            try {
                val userId = UserSessionManager.userId.value
                val username = UserSessionManager.username.value

                val alert = Alert(
                    alertId = UUID.randomUUID().toString(),
                    userId = userId,
                    username = username,
                    message = message,
                    location = locationDescription,
                    timestamp = System.currentTimeMillis(),
                    isActive = true
                )

                withContext(Dispatchers.IO) {
                    database.alertDao().insertAlert(alert)
                }

                _alertSent.value = true
                Log.d("AlertViewModel", "Emergency alert sent by $username")
            } catch (e: Exception) {
                Log.e("AlertViewModel", "Error sending alert: ${e.message}", e)
                _error.value = "Failed to send alert: ${e.message}"
            }
        }
    }

    fun sendTripNotification(tripName: String, tripAction: String, username: String) {
        if (!UserSessionManager.isLoggedIn.value) {
            _error.value = "User must be logged in to send notifications"
            return
        }

        viewModelScope.launch {
            try {
                val userId = UserSessionManager.userId.value

                // Create a notification alert
                val alert = Alert(
                    alertId = UUID.randomUUID().toString(),
                    userId = userId,
                    username = username,
                    message = "$username has $tripAction their trip: $tripName",
                    location = "Trip update notification", // This is just informational
                    timestamp = System.currentTimeMillis(),
                    isActive = true
                )

                withContext(Dispatchers.IO) {
                    database.alertDao().insertAlert(alert)
                }

                Log.d("AlertViewModel", "Trip notification sent by $username")
            } catch (e: Exception) {
                Log.e("AlertViewModel", "Error sending trip notification: ${e.message}", e)
                _error.value = "Failed to send trip notification: ${e.message}"
            }
        }
    }

    fun getActiveAlerts(): LiveData<List<Alert>> {
        val result = MutableLiveData<List<Alert>>()

        viewModelScope.launch {
            try {
                val alerts = withContext(Dispatchers.IO) {
                    database.alertDao().getAllAlerts().filter { it.isActive }
                }
                result.value = alerts
            } catch (e: Exception) {
                Log.e("AlertViewModel", "Error loading alerts: ${e.message}", e)
                result.value = emptyList()
            }
        }

        return result
    }
}