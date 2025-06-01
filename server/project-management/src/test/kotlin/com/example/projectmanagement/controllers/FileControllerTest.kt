package com.example.projectmanagement.controllers

import com.example.projectmanagement.controllers.dto.*
import com.example.projectmanagement.models.File
import com.example.projectmanagement.models.FileType
import com.example.projectmanagement.services.FileService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.fail

class FileControllerTest {
    
    private lateinit var fileController: FileController
    private lateinit var fileService: FileService
    
    @BeforeEach
    fun setup() {
        fileService = mock(FileService::class.java)
        fileController = FileController(fileService)
    }
    
    @Test
    fun `createFile - should create and return file`() {
        val fileDto = FileDto(
            name = "Test File",
            typeId = 1L,
            authorId = 1L
        )
        
        val fileType = FileType(id = 1L, name = "note")
        val file = File(
            id = 1L,
            name = fileDto.name,
            type = fileType,
            authorId = fileDto.authorId,
            uploadDate = LocalDateTime.now()
        )
        
        `when`(fileService.createFile(fileDto)).thenReturn(file)
        
        val result = fileController.createFile(fileDto)
        
        assertNotNull(result)
        assertEquals(file.id, result.id)
        assertEquals(file.name, result.name)
        assertEquals(file.type?.id, result.typeId)
        assertEquals(file.authorId, result.authorId)
    }
    
    @Test
    fun `getFile - should return file by id`() {
        val fileId = 1L
        val fileType = FileType(id = 1L, name = "note")
        val file = File(
            id = fileId,
            name = "Test File",
            type = fileType,
            authorId = 1L,
            uploadDate = LocalDateTime.now()
        )
        
        `when`(fileService.getFileById(fileId)).thenReturn(file)
        
        val result = fileController.getFile(fileId)
        
        assertNotNull(result)
        assertEquals(file.id, result.id)
        assertEquals(file.name, result.name)
    }
    
    @Test
    fun `getFile - when file doesn't exist - should propagate exception`() {
        val fileId = 999L
        `when`(fileService.getFileById(fileId)).thenThrow(
            ResponseStatusException(HttpStatus.NOT_FOUND, "File not found")
        )
        
        try {

            fileController.getFile(fileId)
            fail("Should have thrown ResponseStatusException")
        } catch (e: ResponseStatusException) {

            assertEquals(HttpStatus.NOT_FOUND, e.statusCode)
            assertEquals("File not found", e.reason)
        }
    }
    
    @Test
    fun `updateFile - should update and return updated file`() {
        val fileId = 1L
        val updateDto = FileDto(
            name = "Updated File",
            typeId = 1L,
            authorId = 1L
        )
        
        val fileType = FileType(id = 1L, name = "note")
        val updatedFile = File(
            id = fileId,
            name = updateDto.name,
            type = fileType,
            authorId = updateDto.authorId,
            uploadDate = LocalDateTime.now()
        )
        
        `when`(fileService.updateFile(fileId, updateDto)).thenReturn(updatedFile)
        
        val result = fileController.updateFile(fileId, updateDto)
        
        assertNotNull(result)
        assertEquals(updatedFile.id, result.id)
        assertEquals(updatedFile.name, result.name)
    }
    
    @Test
    fun `updateSuperObjectId - should update super object id`() {
        val fileId = 1L
        val superObjectId = "super123"
        val request = mapOf("superObjectId" to superObjectId)
        
        val fileType = FileType(id = 1L, name = "note")
        val updatedFile = File(
            id = fileId,
            name = "Test File",
            type = fileType,
            authorId = 1L,
            uploadDate = LocalDateTime.now(),
            superObjectId = superObjectId
        )
        
        `when`(fileService.updateSuperObjectId(fileId, superObjectId)).thenReturn(updatedFile)
        
        val result = fileController.updateSuperObjectId(fileId, request)
        
        assertNotNull(result)
        assertEquals(updatedFile.id, result.id)
        assertEquals(superObjectId, result.superObjectId)
    }
    
    @Test
    fun `updateSuperObjectId - should handle null superObjectId`() {
        val fileId = 1L
        val request = mapOf("superObjectId" to null)
        
        val fileType = FileType(id = 1L, name = "note")
        val updatedFile = File(
            id = fileId,
            name = "Test File",
            type = fileType,
            authorId = 1L,
            uploadDate = LocalDateTime.now(),
            superObjectId = null
        )
        
        `when`(fileService.updateSuperObjectId(fileId, null)).thenReturn(updatedFile)
        
        val result = fileController.updateSuperObjectId(fileId, request)
        
        assertNotNull(result)
        assertEquals(updatedFile.id, result.id)
        assertNull(result.superObjectId)
    }
    
    @Test
    fun `updateFileName - should update file name`() {
        val fileId = 1L
        val newName = "Renamed File"
        val dto = FileUpdateNameDto(name = newName)
        
        val fileType = FileType(id = 1L, name = "note")
        val updatedFile = File(
            id = fileId,
            name = newName,
            type = fileType,
            authorId = 1L,
            uploadDate = LocalDateTime.now()
        )
        
        `when`(fileService.updateFileName(fileId, newName)).thenReturn(updatedFile)
        
        val result = fileController.updateFileName(fileId, dto)
        
        assertNotNull(result)
        assertEquals(updatedFile.id, result.id)
        assertEquals(newName, result.name)
    }
    
    @Test
    fun `deleteFile - should delete file`() {
        val fileId = 1L
        
        fileController.deleteFile(fileId)
        
        verify(fileService, times(1)).deleteFile(fileId)
    }
}
