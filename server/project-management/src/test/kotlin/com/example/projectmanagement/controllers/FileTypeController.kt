package com.example.projectmanagement.controllers

import com.example.projectmanagement.models.FileType
import com.example.projectmanagement.services.FileTypeService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.fail

class FileTypeControllerTest {
    
    private lateinit var fileTypeController: FileTypeController
    private lateinit var fileTypeService: FileTypeService
    
    @BeforeEach
    fun setup() {
        fileTypeService = mock(FileTypeService::class.java)
        fileTypeController = FileTypeController(fileTypeService)
    }
    
    @Test
    fun `createType - should create and return file type`() {
        val typeName = "document"
        val fileType = FileType(id = 1L, name = typeName)
        
        `when`(fileTypeService.createType(typeName)).thenReturn(fileType)
        
        val result = fileTypeController.createType(mapOf("name" to typeName))
        
        assertEquals(fileType, result)
        verify(fileTypeService).createType(typeName)
    }
    
    @Test
    fun `createType - when name is missing - should throw exception`() {
        try {

            fileTypeController.createType(mapOf())
            fail("Should have thrown IllegalArgumentException")
        } catch (e: IllegalArgumentException) {

            assertEquals("name required", e.message)
        }
    }
    
    @Test
    fun `getAll - should return all file types`() {
        val fileTypes = listOf(
            FileType(id = 1L, name = "document"),
            FileType(id = 2L, name = "image"),
            FileType(id = 3L, name = "audio")
        )
        
        `when`(fileTypeService.getAllTypes()).thenReturn(fileTypes)
        
        val result = fileTypeController.getAll()
        
        assertEquals(fileTypes, result)
        verify(fileTypeService).getAllTypes()
    }
}
