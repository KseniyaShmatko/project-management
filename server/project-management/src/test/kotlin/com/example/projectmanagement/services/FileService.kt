package com.example.projectmanagement.services

import com.example.projectmanagement.controllers.dto.FileDto
import com.example.projectmanagement.models.File
import com.example.projectmanagement.models.FileType
import com.example.projectmanagement.repositories.FileRepository
import com.example.projectmanagement.repositories.FileTypeRepository
import org.hibernate.Hibernate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class FileServiceTest {

    private lateinit var fileService: FileService
    private lateinit var fileRepository: FileRepository
    private lateinit var fileTypeRepository: FileTypeRepository

    @BeforeEach
    fun setup() {
        fileRepository = mock(FileRepository::class.java)
        fileTypeRepository = mock(FileTypeRepository::class.java)
        fileService = FileService(fileRepository, fileTypeRepository)
    }

    @Test
    fun `createFile - when valid dto - should create and return file`() {
        val fileDto = FileDto(
            name = "Test File",
            typeId = 1L,
            authorId = 1L
        )

        val fileType = FileType(id = 1L, name = "note")

        `when`(fileTypeRepository.findById(fileDto.typeId)).thenReturn(Optional.of(fileType))
        `when`(fileRepository.save(any())).thenAnswer { invocation ->
            val fileArg = invocation.getArgument<File>(0)
            File(
                id = 1L,
                name = fileArg.name,
                type = fileArg.type,
                authorId = fileArg.authorId,
                uploadDate = fileArg.uploadDate
            )
        }

        val result = fileService.createFile(fileDto)

        assertNotNull(result)
        assertEquals(1L, result.id)
        assertEquals(fileDto.name, result.name)
        assertEquals(fileType, result.type)
        assertEquals(fileDto.authorId, result.authorId)

        verify(fileTypeRepository).findById(fileDto.typeId)
        verify(fileRepository).save(any())
    }

    @Test
    fun `createFile - when invalid type id - should throw bad request exception`() {
        val fileDto = FileDto(
            name = "Test File",
            typeId = 999L,
            authorId = 1L
        )

        `when`(fileTypeRepository.findById(fileDto.typeId)).thenReturn(Optional.empty())

        val exception = assertFailsWith<ResponseStatusException> {
            fileService.createFile(fileDto)
        }

        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        assertEquals("Invalid file type", exception.reason)

        verify(fileTypeRepository).findById(fileDto.typeId)
        verify(fileRepository, never()).save(any())
    }

    @Test
    fun `getFileById - when file exists - should return file`() {
        val fileId = 1L
        val fileType = FileType(id = 1L, name = "note")
        val file = File(
            id = fileId,
            name = "Test File",
            type = fileType,
            authorId = 1L,
            uploadDate = LocalDateTime.now()
        )

        `when`(fileRepository.findById(fileId)).thenReturn(Optional.of(file))

        val result = fileService.getFileById(fileId)

        assertSame(file, result)
        verify(fileRepository).findById(fileId)
    }

    @Test
    fun `getFileById - when file doesn't exist - should throw not found exception`() {
        val fileId = 999L

        `when`(fileRepository.findById(fileId)).thenReturn(Optional.empty())

        val exception = assertFailsWith<ResponseStatusException> {
            fileService.getFileById(fileId)
        }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertEquals("File not found", exception.reason)

        verify(fileRepository).findById(fileId)
    }

    @Test
    fun `updateFile - when file exists - should update and return file`() {
        val fileId = 1L
        val updateDto = FileDto(
            name = "Updated File",
            typeId = 2L,
            authorId = 2L
        )

        val existingFileType = FileType(id = 1L, name = "note")
        val newFileType = FileType(id = 2L, name = "document")

        val existingFile = File(
            id = fileId,
            name = "Original File",
            type = existingFileType,
            authorId = 1L,
            uploadDate = LocalDateTime.now()
        )

        val updatedFile = File(
            id = fileId,
            name = updateDto.name,
            type = newFileType,
            authorId = updateDto.authorId,
            uploadDate = existingFile.uploadDate
        )

        `when`(fileRepository.findById(fileId)).thenReturn(Optional.of(existingFile))
        `when`(fileTypeRepository.findById(updateDto.typeId)).thenReturn(Optional.of(newFileType))
        `when`(fileRepository.save(any(File::class.java))).thenReturn(updatedFile)

        val result = fileService.updateFile(fileId, updateDto)

        assertEquals(updatedFile, result)
        
        verify(fileRepository).findById(fileId)
        verify(fileTypeRepository).findById(updateDto.typeId)
        verify(fileRepository).save(any(File::class.java))
    }

    @Test
    fun `updateSuperObjectId - when file exists - should update super object id`() {
        val fileId = 1L
        val superObjectId = "super123"
        val fileType = FileType(id = 1L, name = "note")
        
        val existingFile = File(
            id = fileId,
            name = "Test File",
            type = fileType,
            authorId = 1L,
            uploadDate = LocalDateTime.now(),
            superObjectId = null
        )

        val updatedFile = File(
            id = fileId,
            name = "Test File",
            type = fileType,
            authorId = 1L,
            uploadDate = existingFile.uploadDate,
            superObjectId = superObjectId
        )

        `when`(fileRepository.findById(fileId)).thenReturn(Optional.of(existingFile))
        `when`(fileRepository.save(any(File::class.java))).thenReturn(updatedFile)

        val result = fileService.updateSuperObjectId(fileId, superObjectId)

        assertEquals(updatedFile, result)
        assertEquals(superObjectId, result.superObjectId)
        
        verify(fileRepository).findById(fileId)
        verify(fileRepository).save(any(File::class.java))
    }

    @Test
    fun `updateFileName - when file exists - should update file name`() {
        val fileId = 1L
        val newName = "Renamed File"
        val fileType = FileType(id = 1L, name = "note")
        
        val existingFile = File(
            id = fileId,
            name = "Original File",
            type = fileType,
            authorId = 1L,
            uploadDate = LocalDateTime.now()
        )

        val updatedFile = File(
            id = fileId,
            name = newName,
            type = fileType,
            authorId = 1L,
            uploadDate = existingFile.uploadDate
        )

        `when`(fileRepository.findById(fileId)).thenReturn(Optional.of(existingFile))
        `when`(fileRepository.save(any(File::class.java))).thenReturn(updatedFile)

        val result = fileService.updateFileName(fileId, newName)

        assertEquals(updatedFile, result)
        assertEquals(newName, result.name)
        
        verify(fileRepository).findById(fileId)
        verify(fileRepository).save(any(File::class.java))
    }

    @Test
    fun `deleteFile - when file exists - should delete file`() {
        val fileId = 1L

        `when`(fileRepository.existsById(fileId)).thenReturn(true)

        fileService.deleteFile(fileId)

        verify(fileRepository).existsById(fileId)
        verify(fileRepository).deleteById(fileId)
    }

    @Test
    fun `deleteFile - when file doesn't exist - should throw not found exception`() {
        val fileId = 999L

        `when`(fileRepository.existsById(fileId)).thenReturn(false)

        val exception = assertFailsWith<ResponseStatusException> {
            fileService.deleteFile(fileId)
        }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertEquals("File not found", exception.reason)

        verify(fileRepository).existsById(fileId)
        verify(fileRepository, never()).deleteById(any())
    }
}
