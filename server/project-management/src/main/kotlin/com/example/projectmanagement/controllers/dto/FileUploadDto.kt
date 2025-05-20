package com.example.projectmanagement.controllers.dto

data class FileUploadResponse(
    val success: Int,
    val file: FileDetails
)

data class FileDetails(
    val url: String,
    var name: String? = null,
    var size: Long? = null
)
