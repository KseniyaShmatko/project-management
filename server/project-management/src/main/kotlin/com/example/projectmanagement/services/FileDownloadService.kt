package com.example.projectmanagement.services

import com.example.projectmanagement.models.User
import com.example.projectmanagement.models.ProjectRole
import com.example.projectmanagement.repositories.*
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Paths
import org.springframework.core.io.Resource

@Service
class FileDownloadService(
    private val projectService: ProjectService,
    private val projectFileRepository: ProjectFileRepository,
    private val fileRepository: FileRepository // Нужен для получения File entity с filePath
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


    fun getFileAsResource(projectId: Long, fileId: Long): Resource {
        val currentUser = getCurrentUser()
        projectService.checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.VIEWER, ProjectRole.EDITOR, ProjectRole.OWNER))

        val projectFile = projectFileRepository.findByProject_IdAndFile_Id(projectId, fileId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in this project.")

        val fileEntity = fileRepository.findById(projectFile.file.id)
             .orElseThrow{ ResponseStatusException(HttpStatus.NOT_FOUND, "File data not found.") }


        if (fileEntity.filePath == null) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File path is not set.")
        }

        try {
            val path = Paths.get(fileEntity.filePath!!)
            val resource = UrlResource(path.toUri())
            if (resource.exists() || resource.isReadable) {
                return resource
            } else {
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read file: ${fileEntity.name}")
            }
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not load file: ${e.message}")
        }
    }
}
