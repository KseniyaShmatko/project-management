package com.example.projectmanagement.controllers.dto

import com.example.projectmanagement.models.File
import com.example.projectmanagement.models.FileType
import org.hibernate.Hibernate

data class FileResponseDto(
    val id: Long,
    val name: String,
    val typeId: Long,
    val typeName: String,
    val authorId: Long,
    val uploadDate: String,
    val superObjectId: String?,
    val filePath: String?
)

// Функция преобразования
fun File.toResponseDto(): FileResponseDto {
    val type = if (this.type != null) {
        // Инициализация lazy-loaded поля
        Hibernate.initialize(this.type)
        this.type
    } else null
    
    return FileResponseDto(
        id = this.id,
        name = this.name,
        typeId = type?.id ?: 0,
        typeName = type?.name ?: "",
        authorId = this.authorId,
        uploadDate = this.uploadDate.toString(),
        superObjectId = this.superObjectId,
        filePath = this.filePath
    )
}

data class FileUpdateNameDto(
    val name: String
)
