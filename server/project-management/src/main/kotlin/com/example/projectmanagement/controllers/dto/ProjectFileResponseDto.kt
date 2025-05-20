package com.example.projectmanagement.controllers.dto

import com.example.projectmanagement.models.File

data class ProjectFileResponseDto(
    val id: Long,
    val name: String,
    val type: FileTypeResponseDto,
    val authorId: Long,
    val date: String,
    val superObjectId: String? // MongoDB ID связанного SuperObject
)

data class FileTypeResponseDto(
    val id: Long,
    val name: String
)
