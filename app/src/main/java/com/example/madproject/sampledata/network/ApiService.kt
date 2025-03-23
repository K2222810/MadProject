package com.example.madproject.sampledata.network

import com.example.madproject.sampledata.User
import retrofit2.http.GET

interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<User>
}