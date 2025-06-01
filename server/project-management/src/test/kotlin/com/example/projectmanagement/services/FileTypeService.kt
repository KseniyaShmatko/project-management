package com.example.projectmanagement.services

import com.example.projectmanagement.models.FileType
import com.example.projectmanagement.repositories.FileTypeRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class FileTypeServiceTest {

    private lateinit var fileTypeService: FileTypeService
    private lateinit var fileTypeRepository: FileTypeRepository

    @BeforeEach
    fun setup() {
        fileTypeRepository = mock(FileTypeRepository::class.java)
        fileTypeService = FileTypeService(fileTypeRepository)
    }

    @Test
    fun `createType - should save and return file type`() {
        val typeName = "document"
        val fileType = FileType(id = 1L, name = typeName)

        `when`(fileTypeRepository.save(any())).thenReturn(fileType)

        val result = fileTypeService.createType(typeName)

        assertEquals(fileType, result)
        verify(fileTypeRepository).save(any())
    }

    @Test
    fun `getAllTypes - should return all file types`() {
        val fileTypes = listOf(
            FileType(id = 1L, name = "document"),
            FileType(id = 2L, name = "image"),
            FileType(id = 3L, name = "audio")
        )

        `when`(fileTypeRepository.findAll()).thenReturn(fileTypes)

        val result = fileTypeService.getAllTypes()

        assertEquals(fileTypes, result)
        verify(fileTypeRepository).findAll()
    }

    @Test
    fun `getById - when type exists - should return file type`() {
        val typeId = 1L
        val fileType = FileType(id = typeId, name = "document")

        `when`(fileTypeRepository.findById(typeId)).thenReturn(Optional.of(fileType))

        val result = fileTypeService.getById(typeId)

        assertSame(fileType, result)
        verify(fileTypeRepository).findById(typeId)
    }

    @Test
    fun `getById - when type doesn't exist - should throw not found exception`() {
        val typeId = 999L

        `when`(fileTypeRepository.findById(typeId)).thenReturn(Optional.empty())
        val exception = assertFailsWith<ResponseStatusException> {
            fileTypeService.getById(typeId)
        }

        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        verify(fileTypeRepository).findById(typeId)
    }
}
