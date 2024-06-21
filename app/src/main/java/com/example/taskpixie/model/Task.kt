package com.example.taskpixie.model

data class TaskPayload(
    val title: String,
    val description: String
)

data class Task(
    val id: String,
    val title: String,
    val completed: Boolean
)
