package com.example.projectmanagement.controllers.dto

data class ProjectUserDto(
    val projectId: Long,
    val userId: Long,
    val role: String,
    val permission: String
)

data class ProjectUserView(
    val id: Long,
    val projectId: Long,
    val userId: Long,
    val role: String,
    val permission: String
)
