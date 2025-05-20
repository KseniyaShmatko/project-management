// src/main/kotlin/com/example/projectmanagement/services/ProjectService.kt
package com.example.projectmanagement.services

import com.example.projectmanagement.controllers.dto.* // Импортируем все DTO из пакета
import com.example.projectmanagement.models.* // Импортируем все модели
import com.example.projectmanagement.repositories.ProjectRepository
import com.example.projectmanagement.repositories.ProjectUserRepository
import com.example.projectmanagement.services.ProjectUserService
// Предположим, у вас есть ProjectFileRepository для загрузки деталей файлов, если они не грузятся вместе с Project
// import com.example.projectmanagement.repositories.ProjectFileRepository 
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.NoSuchElementException
import org.springframework.security.access.AccessDeniedException
import org.hibernate.Hibernate
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus 

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val projectUserRepository: ProjectUserRepository,
    private val projectUserService: ProjectUserService 
) {

    private fun getCurrentUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || authentication.principal == "anonymousUser") {
            throw IllegalStateException("User not authenticated or authentication principal is not available.")
        }
        if (authentication.principal !is User) {
            throw IllegalStateException("Authentication principal is not of type User. Actual type: ${authentication.principal?.javaClass?.name}")
        }
        return authentication.principal as User
    }

    fun projectToProjectResponseDto(project: Project, currentUser: User): ProjectResponseDto {
        val participantsList = project.projectUsers.map { pu ->
            // Убедимся, что pu.user не LAZY или инициализирован, если нужен
            ProjectParticipantDto(
                userId = pu.user.id, // pu.user должен быть загружен через JOIN FETCH
                login = pu.user.login,
                name = pu.user.name,
                surname = pu.user.surname,
                photo = pu.user.photo,
                role = pu.role
            )
        }

        val roleOfCurrentUser = if (project.owner?.id == currentUser.id) {
            ProjectRole.OWNER
        } else {
            project.projectUsers.find { it.user.id == currentUser.id }?.role
        }
        
        // Убедимся, что project.owner не LAZY (загружен через JOIN FETCH)
        val ownerDto = project.owner?.toUserResponseDto() 

        return ProjectResponseDto(
            id = project.id,
            name = project.name,
            description = project.description,
            owner = ownerDto,
            projectFiles = project.projectFiles.mapNotNull { 
                // it.file и it.file.type должны быть загружены через JOIN FETCH
                it.toProjectFileResponseDto() 
            },
            participants = participantsList,
            currentUserRole = roleOfCurrentUser
        )
    }

    // --- Функции-мапперы в DTO ---
    private fun User.toUserResponseDto(): UserResponseDto {
        return UserResponseDto(
            id = this.id,
            name = this.name,
            surname = this.surname,
            login = this.login,
            photo = this.photo
        )
    }

    private fun FileType.toFileTypeResponseDto(): FileTypeResponseDto {
        return FileTypeResponseDto(
            id = this.id,
            name = this.name
        )
    }
    
    private fun ProjectFile.toProjectFileResponseDto(): ProjectFileResponseDto {
        // this.file и this.file.type уже должны быть загружены через JOIN FETCH
        if (this.file.type == null) throw IllegalStateException("FileType entity is null for File id ${this.file.id}")

        return ProjectFileResponseDto(
            id = this.file.id, // Обычно ID самого файла, а не ProjectFile
            name = this.file.name,
            type = this.file.type!!.toFileTypeResponseDto(), // this.file.type должен быть НЕ NULL
            authorId = this.file.authorId, // Предполагается, что у File есть authorId
            date = this.file.uploadDate.toString(), // Преобразуем дату в строку (формат можно настроить)
            superObjectId = this.file.superObjectId // Если у File есть это поле
        )
    }

    @Transactional
    fun createProject(projectRequest: Project): ProjectResponseDto {
        val currentUser = getCurrentUser()
        projectRequest.owner = currentUser
        val savedProject = projectRepository.save(projectRequest) // Сначала сохраняем
        projectUserService.addOwnerAsProjectUser(savedProject.id, currentUser.id) // Затем добавляем участника

        // Для возврата полного DTO, загружаем его с деталями
        val detailedProject = projectRepository.findProjectByIdWithDetails(savedProject.id)
            .orElseThrow { ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to reload project after creation") }
        return projectToProjectResponseDto(detailedProject, currentUser)
    }

    @Transactional(readOnly = true)
    fun getProjectsForCurrentUser(): List<ProjectResponseDto> {
        val currentUser = getCurrentUser()
        println("Getting projects for user: ${currentUser.login} (ID: ${currentUser.id})")

        val participatedProjects = projectRepository.findParticipatedProjectsByUserIdWithDetails(currentUser.id)
        println("Found ${participatedProjects.size} participated projects.")
        participatedProjects.forEach { p ->
            println("Participated Project ID: ${p.id}, Name: ${p.name}")
            p.projectUsers.forEach { pu ->
                println("  Participant in participated project: User ID ${pu.user.id}, Role ${pu.role}")
            }
        }
        
        // Пока закомментируем ownedProjects для чистоты эксперимента, если currentUser не владелец
        // val ownedProjects = projectRepository.findOwnedProjectsByUserIdWithDetails(currentUser.id)
        // val allProjects = (ownedProjects + participatedProjects).distinctBy { it.id }
        val allProjects = participatedProjects.distinctBy { it.id } // Для Боба это должно быть эквивалентно
        
        return allProjects.map { project ->
            println("Mapping project ID: ${project.id} for DTO")
            projectToProjectResponseDto(project, currentUser) 
        }
    }


    @Transactional(readOnly = true)
    fun getProjectById(projectId: Long): ProjectResponseDto {
        val currentUser = getCurrentUser()
        // Сначала проверяем доступ (использует projectUserRepository, это нормально)
        checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.VIEWER, ProjectRole.EDITOR, ProjectRole.OWNER))
        
        // Затем загружаем проект со всеми деталями
        val project = projectRepository.findProjectByIdWithDetails(projectId)
            .orElseThrow { NoSuchElementException("Project not found with id: $projectId") }
            
        return projectToProjectResponseDto(project, currentUser)
    }

    @Transactional
    fun updateProject(projectId: Long, updateData: Project): ProjectResponseDto {
        val currentUser = getCurrentUser()
        checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.EDITOR, ProjectRole.OWNER))

        // Загружаем существующую сущность (без деталей, только для обновления полей самого проекта)
        val projectEntity = projectRepository.findById(projectId)
            .orElseThrow { NoSuchElementException("Project not found with id: $projectId") }

        projectEntity.name = updateData.name
        projectEntity.description = updateData.description
        projectRepository.save(projectEntity) // Сохраняем изменения

        // Для возврата полного DTO, загружаем его с деталями
        val detailedProject = projectRepository.findProjectByIdWithDetails(projectEntity.id)
             .orElseThrow { ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to reload project after update") }
        return projectToProjectResponseDto(detailedProject, currentUser)
    }


    @Transactional
    fun deleteProject(projectId: Long) {
        val projectEntity = projectRepository.findById(projectId) // Загружаем сущность для проверки
            .orElseThrow { NoSuchElementException("Project not found with id: $projectId") }
        
        val currentUser = getCurrentUser()
        checkAccessToProject(projectId, currentUser.id, ProjectRole.OWNER)
        // if (projectEntity.owner?.id != currentUser.id) { // Проверяем доступ по сущности
        //     throw AccessDeniedException("User does not have access to this project")
        // }
        projectRepository.deleteById(projectEntity.id)
    }

    fun checkAccessToProject(projectId: Long, userId: Long, requiredRoles: List<ProjectRole>) {
        val project = projectRepository.findById(projectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found") }

        // 1. Проверяем, является ли пользователь владельцем проекта
        if (project.owner?.id == userId) {
            // Владелец всегда имеет доступ, независимо от `requiredRoles` если не указано обратного
            // (например, если для какой-то операции даже OWNER должен иметь явную роль EDITOR).
            // Для большинства случаев, если `ProjectRole.OWNER` есть в `requiredRoles` или подразумевается, этого достаточно.
            if (requiredRoles.contains(ProjectRole.OWNER)) return
            // Если OWNER не в requiredRoles, но мы считаем что он всегда имеет права, то return.
            // Если OWNER может быть ограничен, то нужна более сложная логика.
            // Для простоты сейчас: владелец = полный доступ, если OWNER есть среди требуемых.
        }

        // 2. Проверяем, является ли пользователь участником с нужной ролью
        val projectUser = projectUserRepository.findByProject_IdAndUser_Id(projectId, userId)
        if (projectUser != null && requiredRoles.contains(projectUser.role)) {
            return
        }

        throw AccessDeniedException("User does not have sufficient permissions for this operation on the project.")
    }

    // Перегруженная версия для одной требуемой роли
    fun checkAccessToProject(projectId: Long, userId: Long, requiredRole: ProjectRole) {
        checkAccessToProject(projectId, userId, listOf(requiredRole))
    }
}
