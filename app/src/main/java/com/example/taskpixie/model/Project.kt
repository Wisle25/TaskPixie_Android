package com.example.taskpixie.model

data class ProjectPayload(
    val title: String,
    val detail: String,
    val priority: String,
    val status: String,
    val members: List<String> // List of user IDs
)

data class PreviewProject(
    val id: String,
    val title: String,
)

data class Project(
    val id: String,
    val title: String,
    val detail: String,
    val priority: String,
    val status: String,
    val members: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val tasks: List<PreviewTask>?
)