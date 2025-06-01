package com.example.projectmanagement.controllers.dto

data class LoginRequest(
    val login: String,
    val password: String
)

data class JwtResponse(
    val token: String,
    val id: Long?,
    val login: String?,
    val name: String?,
    val surname: String?,
    val photo: String?
)

data class RegisterRequest(
    val name: String,
    val surname: String,
    val login: String,
    val password: String,
    val photo: String? = null
)
