package com.example.taskpixie.http

import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.PreviewProject
import com.example.taskpixie.model.Project
import com.example.taskpixie.model.ProjectPayload
import com.example.taskpixie.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ProjectService {

    @POST("/projects")
    fun addProject(
        @Header("Authorization") token: String,
        @Body payload: ProjectPayload
    ): Call<ApiResponse<String>>

    @GET("/projects")
    fun getProjects(
        @Header("Authorization") token: String
    ): Call<ApiResponse<List<PreviewProject>>>

    @GET("/projects/{id}")
    fun getProjectDetails(
        @Header("Authorization") token: String,
        @Path("id") projectId: String
    ): Call<ApiResponse<Project>>

    @DELETE("projects/{id}")
    fun deleteProject(
        @Header("Authorization") token: String,
        @Path("id") projectId: String
    ): Call<ApiResponse<Unit>>

    @GET("/projects-member/{id}")
    fun getProjectMembers(@Path("id") projectId: String): Call<ApiResponse<List<User>>>
}
