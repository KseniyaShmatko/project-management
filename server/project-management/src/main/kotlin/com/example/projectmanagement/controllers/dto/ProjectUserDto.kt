package com.example.projectmanagement.controllers.dto

import com.example.projectmanagement.models.ProjectRole
import com.example.projectmanagement.models.ProjectUser

data class ProjectUserDto(
    val projectId: Long,
    val userId: Long,
    val role: ProjectRole,
)

data class ProjectUserView(
    val id: Long,
    val projectId: Long,
    val userId: Long,
    val userLogin: String,
    val userName: String?,
    val userSurname: String?,
    val userPhoto: String?,
    val role: ProjectRole
)

fun ProjectUser.toView(): ProjectUserView {
    return ProjectUserView(
        id = this.id,
        projectId = this.project.id,
        userId = this.user.id,
        userLogin = this.user.login,
        userName = this.user.name,
        userSurname = this.user.surname,
        userPhoto = this.user.photo,
        role = this.role
    )
}