package com.example.taskpixie.http

import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.PreviewTask
import com.example.taskpixie.model.Task
import com.example.taskpixie.model.TaskPayload
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskService {
    @GET("/tasks")
    fun getTasks(@Header("Authorization") token: String): Call<ApiResponse<List<PreviewTask>>>

    @POST("/tasks")
    fun addTask(
        @Header("Authorization") token: String,
        @Body payload: TaskPayload
    ): Call<ApiResponse<String>>

    @GET("/tasks/{id}")
    fun getTaskDetail(@Path("id") taskId: String, @Header("Authorization") token: String): Call<ApiResponse<Task>>

    @PUT("/tasks/{id}")
    fun updateTask(@Header("Authorization") token: String, @Path("id") taskId: String, @Body payload: TaskPayload): Call<ApiResponse<String>>

    @DELETE("/tasks/{id}")
    fun deleteTask(@Path("id") taskId: String, @Header("Authorization") token: String): Call<ApiResponse<String>>
}