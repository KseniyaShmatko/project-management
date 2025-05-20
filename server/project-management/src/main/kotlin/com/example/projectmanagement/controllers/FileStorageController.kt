package com.example.projectmanagement.controllers

import com.example.projectmanagement.controllers.dto.FileUploadResponse
import com.example.projectmanagement.services.FileStorageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/files-storage")
class FileStorageController(private val fileStorageService: FileStorageService) {

    @PostMapping("/upload/image") 
    fun uploadImage(@RequestParam("image") file: MultipartFile): ResponseEntity<FileUploadResponse> {
        try {
            val response = fileStorageService.storeFile(file)
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            val errorResponse = FileUploadResponse(
                success = 0, 
                file = com.example.projectmanagement.controllers.dto.FileDetails(
                    url = "", 
                    name = file.originalFilename
                )
            )
            println("Error uploading file: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
}
