package com.example.taskpixie.http

import com.example.taskpixie.model.ApiResponse
import com.example.taskpixie.model.LoginUserPayload
import com.example.taskpixie.model.RegisterUserPayload
import com.example.taskpixie.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface UserService {
    @POST("/users")
    fun registerUser(@Body payload: RegisterUserPayload): Call<ApiResponse<String>>

    @POST("/auths")
    fun loginUser(@Body payload: LoginUserPayload): Call<ApiResponse<String>>

    @GET("/auths")
    fun getLoggedUser(@Header("Authorization") token: String): Call<ApiResponse<User>>

    @PUT("/users/{id}")
    @Multipart
    fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("confirmPassword") confirmPassword: RequestBody,
        @Part avatar: MultipartBody.Part?
    ): Call<ApiResponse<User>>
}