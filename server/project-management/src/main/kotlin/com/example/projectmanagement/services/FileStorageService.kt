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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@Service
class FileStorageService(
    @Value("\${file.upload-dir:\${user.home}/uploads_data}")
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
        
        val extension = originalFilename.substringAfterLast('.', "")
        val uniqueFilename = "${UUID.randomUUID()}.${extension}".lowercase()

        try {
            if (file.isEmpty) {
                throw RuntimeException("Failed to store empty file.")
            }
            if (uniqueFilename.contains("..")) {
                throw RuntimeException("Cannot store file with relative path outside current directory $uniqueFilename")
            }

            Files.copy(file.inputStream, this.rootLocation.resolve(uniqueFilename), StandardCopyOption.REPLACE_EXISTING)
            
            val baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
            val fullFileUrl = "$baseUrl/uploads/$uniqueFilename"
    
            println("File stored: $uniqueFilename, URL: $fullFileUrl")

            return FileUploadResponse(
                success = 1,
                file = FileDetails(
                    url = fullFileUrl,
                    name = originalFilename,
                    size = file.size
                )
            )
        } catch (e: Exception) {
            println("Failed to store file $uniqueFilename: ${e.message}")
            return FileUploadResponse(
                success = 0,
                file = FileDetails(url = "", name = originalFilename),
            )
        }
    }
}
