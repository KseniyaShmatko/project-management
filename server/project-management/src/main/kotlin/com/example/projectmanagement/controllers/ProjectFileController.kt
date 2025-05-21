package com.example.projectmanagement.controllers

import com.example.projectmanagement.controllers.dto.*
import com.example.projectmanagement.models.File
import com.example.projectmanagement.services.ProjectFileService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/projects/{project_id}/files")
class ProjectFileController(private val projectFileService: ProjectFileService) {
    @PostMapping("/upload")
    fun uploadAndLinkFileToProject(
        @PathVariable("project_id") projectId: Long,
        @RequestParam("file") multipartFile: MultipartFile,
        @RequestParam("type_id") typeId: Long
    ): ResponseEntity<ProjectFileResponseDto> {
        val createdFileDto = projectFileService.uploadAndLinkFile(projectId, multipartFile, typeId)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFileDto)
    }

    @PostMapping("/link")
    fun linkExistingFileToProject(
        @PathVariable("project_id") projectId: Long,
        @RequestParam("file_id") fileId: Long
    ): ResponseEntity<Map<String, Long>> {
        val projectFile = projectFileService.linkExistingFile(projectId, fileId)
        return ResponseEntity.ok(mapOf("project_id" to projectFile.project.id, "file_id" to projectFile.file.id))
    }

    @GetMapping
    fun getFilesForProject(@PathVariable("project_id") projectId: Long): ResponseEntity<List<ProjectFileResponseDto>> {
        return ResponseEntity.ok(projectFileService.getFilesForProjectWithDetails(projectId))
    }

    @GetMapping("/{file_id}")
    fun getFileInProject(
        @PathVariable("project_id") projectId: Long,
        @PathVariable("file_id") fileId: Long
    ): ResponseEntity<ProjectFileResponseDto> {
        val fileDto = projectFileService.getFileDetailsInProject(projectId, fileId)
        return ResponseEntity.ok(fileDto)
    }

    @PatchMapping("/{file_id}/name")
    fun updateFileNameInProject(
        @PathVariable("project_id") projectId: Long,
        @PathVariable("file_id") fileId: Long,
        @RequestBody dto: FileUpdateNameDto
    ): ResponseEntity<ProjectFileResponseDto> {
        val updatedFileDto = projectFileService.updateFileNameInProject(projectId, fileId, dto.name)
        return ResponseEntity.ok(updatedFileDto)
    }
    
    @PatchMapping("/{file_id}/super-object")
    fun updateSuperObjectIdInProject(
        @PathVariable("project_id") projectId: Long,
        @PathVariable("file_id") fileId: Long,
        @RequestBody request: Map<String, String?>
    ): ResponseEntity<ProjectFileResponseDto> {
        val superObjectId = request["superObjectId"]
        val updatedFileDto = projectFileService.updateSuperObjectIdInProject(projectId, fileId, superObjectId)
        return ResponseEntity.ok(updatedFileDto)
    }

    @DeleteMapping("/{file_id}")
    fun unlinkFileFromProject(
        @PathVariable("project_id") projectId: Long,
        @PathVariable("file_id") fileId: Long
    ): ResponseEntity<Void> {
        projectFileService.unlinkFile(projectId, fileId)
        return ResponseEntity.noContent().build()
    }
}
