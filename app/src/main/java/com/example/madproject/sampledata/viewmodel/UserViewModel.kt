package com.example.madproject.sampledata.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import android.util.Log
import com.example.madproject.sampledata.UserRepository
import com.example.madproject.sampledata.network.RetrofitClient
import com.example.madproject.sampledata.User

class UserViewModel : ViewModel() {

    private val userRepository = UserRepository(RetrofitClient.instance)

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val response = userRepository.getUsers()
                Log.d("UserViewModel", "API Response: $response")
                _users.value = response
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching users", e)
            }
        }
    }
}