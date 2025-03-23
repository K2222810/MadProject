package com.example.madproject.sampledata.network

import retrofit2.Call
import retrofit2.http.GET
import com.example.madproject.sampledata.User

interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<User>
}