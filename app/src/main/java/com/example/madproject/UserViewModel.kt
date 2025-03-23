package com.example.madproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madproject.sampledata.User
import com.example.madproject.sampledata.network.RetrofitClient
import kotlinx.coroutines.launch
import android.util.Log

class UserViewModel : ViewModel() {
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val fetchedUsers = RetrofitClient.instance.getUsers()
                _users.value = fetchedUsers
                Log.d("UserViewModel", "Fetched ${fetchedUsers.size} users")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching users: ${e.message}", e)
                _users.value = emptyList()
            }
        }
    }
}