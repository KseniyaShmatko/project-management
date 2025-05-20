package com.example.projectmanagement.services

import com.example.projectmanagement.models.File
import com.example.projectmanagement.controllers.dto.FileDto
import com.example.projectmanagement.repositories.FileRepository
import com.example.projectmanagement.models.FileType
import com.example.projectmanagement.repositories.FileTypeRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime
import org.hibernate.Hibernate

@Service
class FileService(private val fileRepository: FileRepository, private val fileTypeRepository: FileTypeRepository) {

    fun createFile(dto: FileDto): File {
        val type = fileTypeRepository.findById(dto.typeId)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type") }
        val file = File(
            name = dto.name,
            type = type,
            authorId = dto.authorId,
        )
        return fileRepository.save(file)
    }

    fun getFileById(id: Long): File =
        fileRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "File not found")
        }

    fun updateFile(id: Long, update: FileDto): File {
        val existing = fileRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "File not found") }
        val type = fileTypeRepository.findById(update.typeId)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type") }

        Hibernate.initialize(existing.type)

        // Обновляем File, сохраняя неизмененные поля
        val modified = existing.copy(
            name = update.name,
            type = type,
            authorId = update.authorId,
            // Сохраняем существующие значения для полей, которые не должны изменяться
            // или берем новые значения из update, если они есть
            superObjectId = update.superObjectId ?: existing.superObjectId
        )
        return fileRepository.save(modified)
    }
    
    fun updateSuperObjectId(id: Long, superObjectId: String?): File {
        val existing = fileRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "File not found") }
        
        Hibernate.initialize(existing.type)

        // Создаем копию с обновленным superObjectId
        val modified = File(
            id = existing.id,
            name = existing.name,
            type = existing.type,
            authorId = existing.authorId,
            uploadDate = existing.uploadDate,
            superObjectId = superObjectId,
            filePath = existing.filePath
        )
        
        return fileRepository.save(modified)
    }

    fun deleteFile(id: Long) {
        if (fileRepository.existsById(id)) {
            fileRepository.deleteById(id)
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found")
        }
    }

    fun updateFileName(id: Long, newName: String): File {
        val existing = fileRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "File not found") }
        
        Hibernate.initialize(existing.type)
        
        val modified = existing.copy(name = newName)
        return fileRepository.save(modified)
    }
}

@Service
class FileTypeService(private val repo: FileTypeRepository) {
    fun createType(name: String) = repo.save(FileType(name = name))
    fun getAllTypes() = repo.findAll()
    fun getById(id: Long): FileType = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
}