package com.example.madproject.sampledata

import com.example.madproject.sampledata.network.ApiService

class UserRepository(private val apiService: ApiService) {
    suspend fun getUsers(): List<User> {
        return apiService.getUsers()
    }
}