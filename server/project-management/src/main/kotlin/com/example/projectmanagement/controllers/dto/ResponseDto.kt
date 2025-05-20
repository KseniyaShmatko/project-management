// src/main/kotlin/com/example/projectmanagement/controllers/dto/ResponseDto.kt
package com.example.projectmanagement.controllers.dto

import com.example.projectmanagement.models.ProjectRole
import com.example.projectmanagement.models.Project
import com.example.projectmanagement.models.User

data class UserResponseDto(
    val id: Long,
    val name: String,
    val surname: String,
    val login: String,
    val photo: String?
)

data class ProjectParticipantDto( // Новый DTO для участника
    val userId: Long,
    val login: String,
    val name: String?,
    val surname: String?,
    val photo: String?,
    val role: ProjectRole
)

data class ProjectResponseDto(
    val id: Long,
    val name: String,
    val description: String?,
    val owner: UserResponseDto?,
    val projectFiles: List<ProjectFileResponseDto>,
    val participants: List<ProjectParticipantDto>, // Список участников
    val currentUserRole: ProjectRole? // Роль текущего пользователя в этом проекте
)
