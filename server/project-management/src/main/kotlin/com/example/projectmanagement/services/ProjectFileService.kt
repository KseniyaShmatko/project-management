package com.example.projectmanagement.services

import com.example.projectmanagement.controllers.dto.*
import com.example.projectmanagement.models.*
import com.example.projectmanagement.repositories.mongo.SuperObjectRepository
import com.example.projectmanagement.repositories.*
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.util.*
import org.hibernate.Hibernate

@Service
class ProjectFileService(
    private val projectFileRepository: ProjectFileRepository,
    private val projectRepository: ProjectRepository,
    private val fileRepository: FileRepository,
    private val fileTypeRepository: FileTypeRepository,
    private val userRepository: UserRepository, 
    private val projectService: ProjectService,
    private val superObjectRepository: SuperObjectRepository? = null
) {
    private val uploadDir = "uploads"

    init {
        Files.createDirectories(Paths.get(uploadDir))
    }

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


    private fun File.toProjectFileResponseDto(): ProjectFileResponseDto {
        val fileTypeDto = this.type?.let { ft -> FileTypeResponseDto(id = ft.id, name = ft.name) }
            ?: throw IllegalStateException("FileType is null for file ${this.id}")

        return ProjectFileResponseDto(
            id = this.id,
            name = this.name,
            type = fileTypeDto,
            authorId = this.authorId,
            date = this.uploadDate.toString(),
            superObjectId = this.superObjectId
        )
    }


    @Transactional
    fun uploadAndLinkFile(projectId: Long, multipartFile: MultipartFile, typeId: Long): ProjectFileResponseDto {
        val currentUser = getCurrentUser()
        projectService.checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.EDITOR, ProjectRole.OWNER))

        val project = projectRepository.findById(projectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found") }
        val fileType = fileTypeRepository.findById(typeId)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type") }

        val originalFilename = multipartFile.originalFilename ?: "file_${UUID.randomUUID()}"
        val filename = "${UUID.randomUUID()}_${originalFilename.replace("\\s+", "_")}"
        val targetLocation = Paths.get(uploadDir).resolve(filename)
        Files.copy(multipartFile.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)

        val fileEntity = File(
            name = originalFilename,
            type = fileType,
            authorId = currentUser.id,
            uploadDate = LocalDateTime.now(),
            filePath = targetLocation.toString()
        )
        val savedFile = fileRepository.save(fileEntity)

        val projectFile = ProjectFile(project = project, file = savedFile)
        projectFileRepository.save(projectFile)

        return savedFile.toProjectFileResponseDto()
    }

    @Transactional
    fun linkExistingFile(projectId: Long, fileId: Long): ProjectFile {
        val currentUser = getCurrentUser()
        projectService.checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.EDITOR, ProjectRole.OWNER))

        val project = projectRepository.findById(projectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found") }
        val file = fileRepository.findById(fileId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "File to link not found") }

        if (file.authorId != currentUser.id) {
        }
        
        if (projectFileRepository.existsByProject_IdAndFile_Id(projectId, fileId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "File is already linked to this project.")
        }

        val projectFile = ProjectFile(project = project, file = file)
        return projectFileRepository.save(projectFile)
    }
    
    @Transactional(readOnly = true)
    fun getFilesForProjectWithDetails(projectId: Long): List<ProjectFileResponseDto> {
        val currentUser = getCurrentUser()
        projectService.checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.VIEWER, ProjectRole.EDITOR, ProjectRole.OWNER))

        val projectFiles = projectFileRepository.findAllByProject_Id(projectId)
        return projectFiles.map { pf ->
            Hibernate.initialize(pf.file)
            Hibernate.initialize(pf.file.type)
            pf.file.toProjectFileResponseDto()
        }
    }

    @Transactional(readOnly = true)
    fun getFileDetailsInProject(projectId: Long, fileId: Long): ProjectFileResponseDto {
        val currentUser = getCurrentUser()
        projectService.checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.VIEWER, ProjectRole.EDITOR, ProjectRole.OWNER))

        val projectFile = projectFileRepository.findByProject_IdAndFile_Id(projectId, fileId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in this project")
        
        Hibernate.initialize(projectFile.file)
        Hibernate.initialize(projectFile.file.type)
        return projectFile.file.toProjectFileResponseDto()
    }

    @Transactional
    fun updateFileNameInProject(projectId: Long, fileId: Long, newName: String): ProjectFileResponseDto {
        val currentUser = getCurrentUser()
        projectService.checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.EDITOR, ProjectRole.OWNER))

        val file = projectFileRepository.findByProject_IdAndFile_Id(projectId, fileId)?.file
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in this project")
        
        file.name = newName
        val savedFile = fileRepository.save(file)
        Hibernate.initialize(savedFile.type)
        return savedFile.toProjectFileResponseDto()
    }
    
    @Transactional
    fun updateSuperObjectIdInProject(projectId: Long, fileId: Long, superObjectId: String?): ProjectFileResponseDto {
        val currentUser = getCurrentUser()
        projectService.checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.EDITOR, ProjectRole.OWNER))

        val file = projectFileRepository.findByProject_IdAndFile_Id(projectId, fileId)?.file
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in this project")
        
        file.superObjectId = superObjectId
        val savedFile = fileRepository.save(file)
        Hibernate.initialize(savedFile.type)
        return savedFile.toProjectFileResponseDto()
    }

    @Transactional
    fun unlinkFile(projectId: Long, fileId: Long) {
        val currentUser = getCurrentUser()
        projectService.checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.EDITOR, ProjectRole.OWNER))

        val projectFile = projectFileRepository.findByProject_IdAndFile_Id(projectId, fileId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "File is not linked to this project.")
        
        projectFileRepository.delete(projectFile)
    }
}
