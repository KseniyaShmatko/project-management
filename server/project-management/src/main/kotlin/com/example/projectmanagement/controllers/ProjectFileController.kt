package com.example.projectmanagement.controllers

import com.example.projectmanagement.controllers.dto.* // Все DTO
import com.example.projectmanagement.models.File // Сущность File
import com.example.projectmanagement.services.ProjectFileService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile // Для загрузки файлов

@RestController
@RequestMapping("/projects/{project_id}/files") // Меняем базовый путь для всех операций с файлами проекта
class ProjectFileController(private val projectFileService: ProjectFileService) {

    // POST /projects/{project_id}/files/upload  (Загрузка и немедленная привязка нового файла)
    @PostMapping("/upload")
    fun uploadAndLinkFileToProject(
        @PathVariable("project_id") projectId: Long,
        @RequestParam("file") multipartFile: MultipartFile, // Сам файл
        @RequestParam("type_id") typeId: Long // ID типа файла
        // Можно передавать и name файла от клиента, либо брать из multipartFile.originalFilename
    ): ResponseEntity<ProjectFileResponseDto> {
        // ProjectFileService должен будет:
        // 1. Проверить права пользователя на projectId (EDITOR, OWNER)
        // 2. Сохранить multipartFile на диск/в хранилище
        // 3. Создать запись File в БД (с filePath, authorId = currentUser.id, typeId)
        // 4. Создать запись ProjectFile, связывающую этот File с projectId
        // 5. Вернуть ProjectFileResponseDto
        val createdFileDto = projectFileService.uploadAndLinkFile(projectId, multipartFile, typeId)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFileDto)
    }

    // POST /projects/{project_id}/files/link (Привязка уже существующего файла к проекту)
    @PostMapping("/link")
    fun linkExistingFileToProject(
        @PathVariable("project_id") projectId: Long,
        @RequestParam("file_id") fileId: Long
    ): ResponseEntity<Map<String, Long>> { // Или ProjectFileResponseDto
        // ProjectFileService должен будет:
        // 1. Проверить права пользователя на projectId (EDITOR, OWNER)
        // 2. Проверить, что  текущий пользователь имеет право "делиться" этим fileId (например, он его автор)
        // 3. Создать запись ProjectFile
        val projectFile = projectFileService.linkExistingFile(projectId, fileId)
        return ResponseEntity.ok(mapOf("project_id" to projectFile.project.id, "file_id" to projectFile.file.id))
    }

    // GET /projects/{project_id}/files (Получение списка файлов проекта)
    @GetMapping
    fun getFilesForProject(@PathVariable("project_id") projectId: Long): ResponseEntity<List<ProjectFileResponseDto>> {
        // ProjectFileService:
        // 1. Проверить права на projectId (VIEWER, EDITOR, OWNER)
        // 2. Получить файлы и смапить в DTO
        return ResponseEntity.ok(projectFileService.getFilesForProjectWithDetails(projectId))
    }

    // GET /projects/{project_id}/files/{file_id} (Получение конкретного файла проекта)
    // Этот эндпоинт может быть не так нужен, если getFilesForProject дает все детали.
    // Но полезен, если нужно проверить доступ к конкретному файлу в контексте проекта.
    @GetMapping("/{file_id}")
    fun getFileInProject(
        @PathVariable("project_id") projectId: Long,
        @PathVariable("file_id") fileId: Long
    ): ResponseEntity<ProjectFileResponseDto> {
        // ProjectFileService:
        // 1. Проверить права на projectId (VIEWER, EDITOR, OWNER)
        // 2. Убедиться, что fileId действительно привязан к projectId
        // 3. Вернуть DTO
        val fileDto = projectFileService.getFileDetailsInProject(projectId, fileId)
        return ResponseEntity.ok(fileDto)
    }

    // PATCH /projects/{project_id}/files/{file_id}/name (Переименование файла в проекте)
    @PatchMapping("/{file_id}/name")
    fun updateFileNameInProject(
        @PathVariable("project_id") projectId: Long,
        @PathVariable("file_id") fileId: Long,
        @RequestBody dto: FileUpdateNameDto
    ): ResponseEntity<ProjectFileResponseDto> {
        // ProjectFileService:
        // 1. Проверить права на projectId (EDITOR, OWNER)
        // 2. Убедиться, что fileId привязан к projectId
        // 3. Обновить имя файла (File.name)
        // 4. Вернуть обновленный DTO
        val updatedFileDto = projectFileService.updateFileNameInProject(projectId, fileId, dto.name)
        return ResponseEntity.ok(updatedFileDto)
    }
    
    // PATCH /projects/{project_id}/files/{file_id}/super-object (Обновление superObjectId для файла в проекте)
    @PatchMapping("/{file_id}/super-object")
    fun updateSuperObjectIdInProject(
        @PathVariable("project_id") projectId: Long,
        @PathVariable("file_id") fileId: Long,
        @RequestBody request: Map<String, String?>
    ): ResponseEntity<ProjectFileResponseDto> {
        // ProjectFileService:
        // 1. Проверить права на projectId (EDITOR, OWNER)
        // 2. Убедиться, что fileId привязан к projectId
        // 3. Обновить superObjectId
        // 4. Вернуть DTO
        val superObjectId = request["superObjectId"]
        val updatedFileDto = projectFileService.updateSuperObjectIdInProject(projectId, fileId, superObjectId)
        return ResponseEntity.ok(updatedFileDto)
    }


    // DELETE /projects/{project_id}/files/{file_id} (Отвязка файла от проекта)
    @DeleteMapping("/{file_id}")
    fun unlinkFileFromProject(
        @PathVariable("project_id") projectId: Long,
        @PathVariable("file_id") fileId: Long
    ): ResponseEntity<Void> {
        // ProjectFileService:
        // 1. Проверить права на projectId (EDITOR, OWNER)
        // 2. Удалить запись ProjectFile (не сам файл из БД, если он может использоваться в других проектах)
        //    Или, если файл создавался *только* для этого проекта, можно и удалить сам File (требует логики)
        projectFileService.unlinkFile(projectId, fileId)
        return ResponseEntity.noContent().build()
    }
}
