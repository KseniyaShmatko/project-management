package com.example.projectmanagement.services

import com.example.projectmanagement.models.File
import com.example.projectmanagement.controllers.dto.FileDto
import com.example.projectmanagement.repositories.FileRepository
import com.example.projectmanagement.models.FileType
import com.example.projectmanagement.repositories.FileTypeRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class FileService(private val fileRepository: FileRepository, private val fileTypeRepository: FileTypeRepository) {

    fun createFile(dto: FileDto): File {
        val type = fileTypeRepository.findById(dto.typeId)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type") }
        val file = File(
            name = dto.name,
            type = type,
            author = dto.author,
            date = dto.date
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

        val modified = existing.copy(
            name = update.name,
            type = type,
            author = update.author,
            date = update.date
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
}

@Service
class FileTypeService(private val repo: FileTypeRepository) {
    fun createType(name: String) = repo.save(FileType(name = name))
    fun getAllTypes() = repo.findAll()
    fun getById(id: Long): FileType = repo.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
}