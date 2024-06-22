package com.example.taskpixie.model

data class RegisterUserPayload(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String
)

data class LoginUserPayload(
    val identity: String,
    val password: String
)

data class User(
    val id: String,
    val username: String,
    val email: String,
    val avatarLink: String,
) {
    override fun toString(): String {
        return username
    }
}
