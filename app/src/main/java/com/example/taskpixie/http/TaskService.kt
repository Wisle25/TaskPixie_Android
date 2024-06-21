package com.example.taskpixie.http

import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.Task
import com.example.taskpixie.model.TaskPayload
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface TaskService {
    @GET("/tasks/user/{userId}")
    fun getTasksByUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: String
    ): Call<ApiResponse<List<Task>>>

    @POST("/tasks")
    fun addTask(
        @Header("Authorization") token: String,
        @Body payload: TaskPayload
    ): Call<ApiResponse<String>>
}