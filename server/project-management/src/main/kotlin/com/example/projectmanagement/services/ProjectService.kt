package com.example.projectmanagement.services

import com.example.projectmanagement.models.Project
import com.example.projectmanagement.repositories.ProjectRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ProjectService(private val projectRepository: ProjectRepository) {

    fun createProject(project: Project): Project =
        projectRepository.save(project)

    fun getProjectById(id: Long): Project =
        projectRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found")
        }

    fun updateProject(id: Long, update: Project): Project {
        val existing = projectRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found") }
        val modified = existing.copy(
            name = update.name,
            date = update.date,
            author = update.author
        )
        return projectRepository.save(modified)
    }

    fun deleteProject(id: Long) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id)
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found")
        }
    }
}
