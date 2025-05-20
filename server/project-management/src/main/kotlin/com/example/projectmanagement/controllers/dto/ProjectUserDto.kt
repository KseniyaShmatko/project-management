package com.example.projectmanagement.controllers.dto

import com.example.projectmanagement.models.ProjectRole
import com.example.projectmanagement.models.ProjectUser

data class ProjectUserDto(
    val projectId: Long,
    val userId: Long,
    val role: ProjectRole,
)

data class ProjectUserView(
    val id: Long, // ID самой связи ProjectUser
    val projectId: Long,
    val userId: Long,
    val userLogin: String, // Добавим для удобства фронтенда
    val userName: String?,  // Добавим для удобства фронтенда
    val userSurname: String?,// Добавим для удобства фронтенда
    val userPhoto: String?, // Добавим для удобства фронтенда
    val role: ProjectRole
)

// Можно добавить маппер в сервисе или прямо тут
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