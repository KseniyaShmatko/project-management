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

data class ProjectParticipantDto(
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
    val participants: List<ProjectParticipantDto>,
    val currentUserRole: ProjectRole?
)
