package com.example.projectmanagement.services

import com.example.projectmanagement.models.ProjectUser
import com.example.projectmanagement.repositories.ProjectUserRepository
import com.example.projectmanagement.repositories.ProjectRepository
import com.example.projectmanagement.repositories.UserRepository
import com.example.projectmanagement.controllers.dto.ProjectUserDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ProjectUserService(
    private val projectUserRepository: ProjectUserRepository,
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository
) {
    fun linkUserToProject(dto: ProjectUserDto): ProjectUser {
        val project = projectRepository.findById(dto.projectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found") }
        val user = userRepository.findById(dto.userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
        return projectUserRepository.save(
            ProjectUser(
                project = project,
                user = user,
                role = dto.role,
                permission = dto.permission
            )
        )
    }

    fun getProjectsForUser(userId: Long): List<ProjectUser> =
        projectUserRepository.findAllByUser_Id(userId)

    fun getUsersForProject(projectId: Long): List<ProjectUser> =
        projectUserRepository.findAllByProject_Id(projectId)

    fun updateRoleAndPermission(projectId: Long, userId: Long, role: String, permission: String): ProjectUser {
        val projectUser = projectUserRepository.findByProject_IdAndUser_Id(projectId, userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Link not found")
        projectUser.role = role
        projectUser.permission = permission
        return projectUserRepository.save(projectUser)
    }
}
