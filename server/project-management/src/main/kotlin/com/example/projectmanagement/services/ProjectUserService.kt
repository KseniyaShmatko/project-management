package com.example.projectmanagement.services

import com.example.projectmanagement.models.ProjectUser
import com.example.projectmanagement.repositories.ProjectUserRepository
import com.example.projectmanagement.repositories.ProjectRepository
import com.example.projectmanagement.repositories.UserRepository
import com.example.projectmanagement.controllers.dto.ProjectUserDto
import com.example.projectmanagement.models.ProjectRole
import com.example.projectmanagement.models.User
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.transaction.annotation.Transactional

@Service
class ProjectUserService(
    private val projectUserRepository: ProjectUserRepository,
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository
) {

    private fun getCurrentUser(): User {
        val principal = SecurityContextHolder.getContext().authentication.principal
        if (principal is User) {
            return principal
        }
        throw IllegalStateException("User not authenticated or principal is not User type")
    }

    private fun checkPermissionToManageProjectUsers(projectId: Long, manager: User) {
        val project = projectRepository.findById(projectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found") }

        // Владелец проекта может управлять участниками
        if (project.owner?.id == manager.id) return

        // Или участник с ролью OWNER (если используется явная запись для владельца)
        // или в будущем, если появится роль типа PROJECT_ADMIN
        val managerLink = projectUserRepository.findByProject_IdAndUser_Id(projectId, manager.id)
        if (managerLink?.role != ProjectRole.OWNER) { // Пока только OWNER может управлять этим
            throw AccessDeniedException("User does not have permission to manage users for this project")
        }
    }

    @Transactional
    fun linkUserToProject(dto: ProjectUserDto): ProjectUser {
        val currentUser = getCurrentUser()
        checkPermissionToManageProjectUsers(dto.projectId, currentUser)

        val project = projectRepository.findById(dto.projectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found") }
        val userToLink = userRepository.findById(dto.userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User to link not found") }

        if (project.owner?.id == userToLink.id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot link project owner as a regular user. Owner has implicit full access or an OWNER role.")
        }

        if (dto.role == ProjectRole.OWNER) {
             throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot assign OWNER role. This role is reserved for the project creator.")
        }

        projectUserRepository.findByProject_IdAndUser_Id(dto.projectId, dto.userId)?.let {
            throw ResponseStatusException(HttpStatus.CONFLICT, "User is already linked to this project")
        }

        return projectUserRepository.save(
            ProjectUser(
                project = project,
                user = userToLink,
                role = dto.role
            )
        )
    }
    
    // Используется для добавления владельца как участника при создании проекта
    // Вызывается из ProjectService
    @Transactional
    fun addOwnerAsProjectUser(projectId: Long, ownerId: Long): ProjectUser {
         val project = projectRepository.findById(projectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found") }
        val owner = userRepository.findById(ownerId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Owner user not found") }

        return projectUserRepository.save(
            ProjectUser(project = project, user = owner, role = ProjectRole.OWNER)
        )
    }


    fun getProjectsForUser(userId: Long): List<ProjectUser> =
        projectUserRepository.findAllByUser_Id(userId) // Здесь user.id - это ID пользователя, для которого ищем проекты

    fun getUsersForProject(projectId: Long): List<ProjectUser> {
        // Можно добавить проверку, что текущий пользователь имеет право видеть участников этого проекта
        // Например, он должен быть владельцем или участником этого проекта
        val currentUser = getCurrentUser()
        projectRepository.findById(projectId).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found") }
        
        val isOwner = projectRepository.existsByIdAndOwner_Id(projectId, currentUser.id)
        val isParticipant = projectUserRepository.existsByProject_IdAndUser_Id(projectId, currentUser.id)

        if (!isOwner && !isParticipant) {
             throw AccessDeniedException("User does not have permission to view participants for this project")
        }
        return projectUserRepository.findAllByProject_Id(projectId)
    }


    @Transactional
    fun updateUserProjectRole(projectId: Long, userIdToUpdate: Long, newRole: ProjectRole): ProjectUser {
        val currentUser = getCurrentUser()
        checkPermissionToManageProjectUsers(projectId, currentUser)

        val projectUser = projectUserRepository.findByProject_IdAndUser_Id(projectId, userIdToUpdate)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Link between user and project not found")

        if (projectUser.user.id == projectUser.project.owner?.id && newRole != ProjectRole.OWNER) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change the role of the project owner from OWNER.")
        }
         if (newRole == ProjectRole.OWNER && projectUser.user.id != projectUser.project.owner?.id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot assign OWNER role to a non-owner user.")
        }


        projectUser.role = newRole
        return projectUserRepository.save(projectUser)
    }

    @Transactional
    fun removeUserFromProject(projectId: Long, userIdToRemove: Long) {
        val currentUser = getCurrentUser()
        checkPermissionToManageProjectUsers(projectId, currentUser)

        val projectUser = projectUserRepository.findByProject_IdAndUser_Id(projectId, userIdToRemove)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User is not part of this project.")

        if (projectUser.role == ProjectRole.OWNER) {
             throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove the project owner. To remove the owner, delete the project or transfer ownership (if implemented).")
        }

        projectUserRepository.delete(projectUser)
    }
}
