package com.example.projectmanagement.services

import com.example.projectmanagement.controllers.dto.*
import com.example.projectmanagement.models.* 
import com.example.projectmanagement.repositories.ProjectRepository
import com.example.projectmanagement.repositories.ProjectUserRepository
import com.example.projectmanagement.services.ProjectUserService
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
            ProjectParticipantDto(
                userId = pu.user.id,
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
        
        val ownerDto = project.owner?.toUserResponseDto() 

        return ProjectResponseDto(
            id = project.id,
            name = project.name,
            description = project.description,
            owner = ownerDto,
            projectFiles = project.projectFiles.mapNotNull { 
                it.toProjectFileResponseDto() 
            },
            participants = participantsList,
            currentUserRole = roleOfCurrentUser
        )
    }

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
        if (this.file.type == null) throw IllegalStateException("FileType entity is null for File id ${this.file.id}")

        return ProjectFileResponseDto(
            id = this.file.id,
            name = this.file.name,
            type = this.file.type!!.toFileTypeResponseDto(),
            authorId = this.file.authorId,
            date = this.file.uploadDate.toString(), 
            superObjectId = this.file.superObjectId
        )
    }

    @Transactional
    fun createProject(projectRequest: Project): ProjectResponseDto {
        val currentUser = getCurrentUser()
        projectRequest.owner = currentUser
        val savedProject = projectRepository.save(projectRequest)
        projectUserService.addOwnerAsProjectUser(savedProject.id, currentUser.id)
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
        
        val allProjects = participatedProjects.distinctBy { it.id }
        
        return allProjects.map { project ->
            println("Mapping project ID: ${project.id} for DTO")
            projectToProjectResponseDto(project, currentUser) 
        }
    }


    @Transactional(readOnly = true)
    fun getProjectById(projectId: Long): ProjectResponseDto {
        val currentUser = getCurrentUser()
        checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.VIEWER, ProjectRole.EDITOR, ProjectRole.OWNER))
        
        val project = projectRepository.findProjectByIdWithDetails(projectId)
            .orElseThrow { NoSuchElementException("Project not found with id: $projectId") }
            
        return projectToProjectResponseDto(project, currentUser)
    }

    @Transactional
    fun updateProject(projectId: Long, updateData: Project): ProjectResponseDto {
        val currentUser = getCurrentUser()
        checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.EDITOR, ProjectRole.OWNER))

        val projectEntity = projectRepository.findById(projectId)
            .orElseThrow { NoSuchElementException("Project not found with id: $projectId") }

        projectEntity.name = updateData.name
        projectEntity.description = updateData.description
        projectRepository.save(projectEntity)

        val detailedProject = projectRepository.findProjectByIdWithDetails(projectEntity.id)
             .orElseThrow { ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to reload project after update") }
        return projectToProjectResponseDto(detailedProject, currentUser)
    }


    @Transactional
    fun deleteProject(projectId: Long) {
        val projectEntity = projectRepository.findById(projectId)
            .orElseThrow { NoSuchElementException("Project not found with id: $projectId") }
        
        val currentUser = getCurrentUser()
        checkAccessToProject(projectId, currentUser.id, ProjectRole.OWNER)
        projectRepository.deleteById(projectEntity.id)
    }

    fun checkAccessToProject(projectId: Long, userId: Long, requiredRoles: List<ProjectRole>) {
        val project = projectRepository.findById(projectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found") }

        if (project.owner?.id == userId) {
            if (requiredRoles.contains(ProjectRole.OWNER)) return
        }

        val projectUser = projectUserRepository.findByProject_IdAndUser_Id(projectId, userId)
        if (projectUser != null && requiredRoles.contains(projectUser.role)) {
            return
        }

        throw AccessDeniedException("User does not have sufficient permissions for this operation on the project.")
    }

    fun checkAccessToProject(projectId: Long, userId: Long, requiredRole: ProjectRole) {
        checkAccessToProject(projectId, userId, listOf(requiredRole))
    }
}
