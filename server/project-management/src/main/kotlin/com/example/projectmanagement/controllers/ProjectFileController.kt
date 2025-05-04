package com.example.projectmanagement.controllers

import com.example.projectmanagement.models.ProjectFile
import com.example.projectmanagement.models.File
import com.example.projectmanagement.services.ProjectFileService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/projects/{project_id}")
class ProjectFileController(private val projectFileService: ProjectFileService) {
    @PostMapping("/add_file")
    fun linkFileToProject(
        @PathVariable("project_id") projectId: Long,
        @RequestParam("file_id") fileId: Long
    ): Map<String, Long> {
        val projectFile = projectFileService.linkFileToProject(projectId, fileId)
        return mapOf("project_id" to projectFile.project.id, "file_id" to projectFile.file.id)
    }

    @GetMapping("/files")
    fun getFilesForProject(@PathVariable project_id: Long): List<File> =
        projectFileService.getFilesForProject(project_id)

}
