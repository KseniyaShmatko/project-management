// src/main/kotlin/com/example/projectmanagement/services/FileStorageService.kt
package com.example.projectmanagement.services

import com.example.projectmanagement.controllers.dto.FileDetails
import com.example.projectmanagement.controllers.dto.FileUploadResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@Service
class FileStorageService(
    // Тот же самый uploadDir, что и в WebConfig
    @Value("\${file.upload-dir:\${user.home}/editorjs_uploads}")
    private val uploadDir: String
) {

    private val rootLocation = Paths.get(uploadDir)

    init {
        try {
            if (Files.notExists(rootLocation)) {
                Files.createDirectories(rootLocation)
            }
        } catch (e: Exception) {
            throw RuntimeException("Could not initialize storage directory!", e)
        }
    }

    fun storeFile(file: MultipartFile): FileUploadResponse {
        val originalFilename = file.originalFilename 
            ?: throw IllegalArgumentException("File name cannot be null")
        
        // Генерируем уникальное имя файла, чтобы избежать коллизий
        val extension = originalFilename.substringAfterLast('.', "")
        val uniqueFilename = "${UUID.randomUUID()}.${extension}".lowercase()

        try {
            if (file.isEmpty) {
                throw RuntimeException("Failed to store empty file.")
            }
            // Проверка на недопустимые символы в имени файла (для безопасности)
            if (uniqueFilename.contains("..")) {
                throw RuntimeException("Cannot store file with relative path outside current directory $uniqueFilename")
            }

            Files.copy(file.inputStream, this.rootLocation.resolve(uniqueFilename), StandardCopyOption.REPLACE_EXISTING)
            
            // Формируем URL, по которому файл будет доступен
            // Важно: этот URL должен соответствовать тому, что настроено в WebConfig (resourcePath)
            // Если есть context-path, его тоже нужно учесть
            // Предположим, сервер работает на localhost:8080, context-path нет
            val fileUrl = "/uploads/$uniqueFilename" // Относительный URL
            // Для полного URL, если нужно клиенту:
            // val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
            // val fullFileUrl = "$baseUrl/uploads/$uniqueFilename"


            println("File stored: $uniqueFilename, URL: $fileUrl")

            return FileUploadResponse(
                success = 1,
                file = FileDetails(
                    url = fileUrl, // Отдаем относительный URL, клиент сам добавит домен
                    name = originalFilename,
                    size = file.size
                )
            )
        } catch (e: Exception) {
            println("Failed to store file $uniqueFilename: ${e.message}")
            // В реальном приложении здесь должна быть более детальная обработка ошибок
            throw RuntimeException("Failed to store file $uniqueFilename", e) 
            // Или вернуть FileUploadResponse с success = 0
            // return FileUploadResponse(success = 0, file = FileDetails(url = "", name = originalFilename), message = e.message)
        }
    }
}
