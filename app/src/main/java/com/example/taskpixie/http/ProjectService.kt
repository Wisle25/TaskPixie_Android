package com.example.taskpixie.http

import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.Project
import com.example.taskpixie.model.ProjectPayload
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ProjectService {
    @GET("/projects")
    fun getProjects(@Header("Authorization") token: String): Call<ApiResponse<List<Project>>>

    @POST("/projects")
    fun addProject(@Header("Authorization") token: String, @Body payload: ProjectPayload): Call<ApiResponse<Project>>
}
