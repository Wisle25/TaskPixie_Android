package com.example.taskpixie.model

data class TaskPayload(
    val title: String,
    val description: String
)

data class PreviewTask(
    val id: String,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val project: String?,
)

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val detail: String,
    val priority: String,
    val project: String?,
    val assignedTo: List<String>?,
    val dueDate: String?,
    val status: String
)