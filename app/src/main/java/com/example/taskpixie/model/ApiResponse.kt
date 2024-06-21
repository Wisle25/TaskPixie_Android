package com.example.taskpixie.model

data class ApiResponse<T>(
    val status: String,
    val message: String?,
    val data: T?
)
