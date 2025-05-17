// src/main/kotlin/com/example/projectmanagement/controllers/dto/FileUploadDto.kt
package com.example.projectmanagement.controllers.dto

data class FileUploadResponse(
    val success: Int, // 1 для успеха, 0 для ошибки
    val file: FileDetails
)

data class FileDetails(
    val url: String, // URL к загруженному файлу
    // Можно добавить другие поля, если нужны: name, size, etc.
    var name: String? = null,
    var size: Long? = null
)
