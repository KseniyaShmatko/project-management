package com.example.projectmanagement.controllers.dto

data class FileDto(
    val name: String,
    val typeId: Long,
    val authorId: Long,
    val superObjectId: String? = null
)
