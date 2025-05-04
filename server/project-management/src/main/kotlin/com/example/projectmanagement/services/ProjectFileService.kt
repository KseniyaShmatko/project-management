package com.example.projectmanagement.services

import com.example.projectmanagement.models.ProjectFile
import com.example.projectmanagement.models.File
import com.example.projectmanagement.repositories.ProjectRepository
import com.example.projectmanagement.repositories.ProjectFileRepository
import com.example.projectmanagement.repositories.FileRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ProjectFileService(
    private val projectFileRepository: ProjectFileRepository,
    private val projectRepository: ProjectRepository,
    private val fileRepository: FileRepository
) {
    fun linkFileToProject(projectId: Long, fileId: Long): ProjectFile {
        val project = projectRepository.findById(projectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found") }
        val file = fileRepository.findById(fileId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "File not found") }
        return projectFileRepository.save(ProjectFile(project = project, file = file))
    }

    fun getFilesForProject(projectId: Long): List<File> {
        val projectFiles = projectFileRepository.findAllByProject_Id(projectId)
        return projectFiles.map { it.file }
    }

}
