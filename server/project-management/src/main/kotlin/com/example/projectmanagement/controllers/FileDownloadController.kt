package com.example.projectmanagement.controllers

import com.example.projectmanagement.services.FileDownloadService // Новый сервис
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/projects/{project_id}/files/{file_id}/download")
class FileDownloadController(private val fileDownloadService: FileDownloadService) {

    @GetMapping
    fun downloadFile(
        @PathVariable project_id: Long,
        @PathVariable file_id: Long
    ): ResponseEntity<Resource> {
        val resource = fileDownloadService.getFileAsResource(project_id, file_id)
        val filename = resource.filename ?: "downloaded_file" // Получить оригинальное имя файла

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM) // или определить тип по расширению файла
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .body(resource)
    }
}
