// src/main/kotlin/com/example/projectmanagement/controllers/FileController.kt
package com.example.projectmanagement.controllers

import com.example.projectmanagement.controllers.dto.FileUploadResponse
import com.example.projectmanagement.services.FileStorageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/files-storage") // Или любой другой путь, например /api/files
class FileStorageController(private val fileStorageService: FileStorageService) {

    @PostMapping("/upload/image") // Или просто /upload, если будете проверять тип файла в сервисе
    fun uploadImage(@RequestParam("image") file: MultipartFile): ResponseEntity<FileUploadResponse> {
        // @RequestParam("image") - "image" должно совпадать с именем поля в FormData на клиенте
        try {
            val response = fileStorageService.storeFile(file)
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            // В реальном приложении лучше возвращать структурированный ответ об ошибке
            // Здесь для простоты вернем ошибку сервера, но FileUploadResponse с success = 0 был бы лучше
            // для обработки на клиенте плагином @editorjs/image.

            // Пример возврата ошибки, как ожидает @editorjs/image
            val errorResponse = FileUploadResponse(
                success = 0, 
                file = com.example.projectmanagement.controllers.dto.FileDetails(
                    url = "", 
                    name = file.originalFilename
                )
                // Вы можете добавить поле message в FileDetails или FileUploadResponse
                // message = e.message ?: "Unknown error during file upload"
            )
            println("Error uploading file: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
}
