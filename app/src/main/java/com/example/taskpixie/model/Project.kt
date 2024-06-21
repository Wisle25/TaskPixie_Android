package com.example.taskpixie.model

data class ProjectPayload(
    val name: String,
    val description: String
)

data class Project(
    val id: String,
    val name: String,
    val description: String,
    val ownerId: String
)