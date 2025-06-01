package com.example.projectmanagement.controllers

import com.example.projectmanagement.controllers.dto.EditorJsBlockDto
import com.example.projectmanagement.models.mongo.SuperObject
import com.example.projectmanagement.services.SuperObjectService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.fail

class SuperObjectControllerTest {
    
    private lateinit var superObjectController: SuperObjectController
    private lateinit var superObjectService: SuperObjectService
    
    @BeforeEach
    fun setup() {
        superObjectService = mock(SuperObjectService::class.java)
        superObjectController = SuperObjectController(superObjectService)
    }
    
    @Test
    fun `create - should create and return super object`() {
        val superObject = SuperObject(
            fileId = 1L,
            serviceType = "NOTE",
            name = "Test Super Object"
        )
        
        val createdSuperObject = SuperObject(
            id = "super123",
            fileId = 1L,
            serviceType = "NOTE",
            name = "Test Super Object"
        )
        
        `when`(superObjectService.create(superObject)).thenReturn(createdSuperObject)
        
        val result = superObjectController.create(superObject)
        
        assertEquals(createdSuperObject, result)
    }
    
    @Test
    fun `getByFileId - should return super object by file id`() {
        val fileId = 1L
        val superObject = SuperObject(
            id = "super123",
            fileId = fileId,
            serviceType = "NOTE",
            name = "Test Super Object"
        )
        
        `when`(superObjectService.getByFileId(fileId)).thenReturn(superObject)
        
        val result = superObjectController.getByFileId(fileId)
        
        assertEquals(superObject, result)
    }
    
    @Test
    fun `getById - should return super object by id`() {
        val superObjectId = "super123"
        val superObject = SuperObject(
            id = superObjectId,
            fileId = 1L,
            serviceType = "NOTE",
            name = "Test Super Object"
        )
        
        `when`(superObjectService.getById(superObjectId)).thenReturn(superObject)
        
        val result = superObjectController.getById(superObjectId)
        
        assertEquals(superObject, result)
    }
    
    @Test
    fun `update - should update and return super object`() {
        val superObjectId = "super123"
        val superObject = SuperObject(
            name = "Updated Super Object"
        )
        
        val updatedSuperObject = SuperObject(
            id = superObjectId,
            fileId = 1L,
            serviceType = "NOTE",
            name = "Updated Super Object"
        )
        
        `when`(superObjectService.update(superObjectId, superObject)).thenReturn(updatedSuperObject)
        
        val result = superObjectController.update(superObjectId, superObject)
        
        assertEquals(updatedSuperObject, result)
    }
    
    @Test
    fun `delete - should delete super object`() {
        val superObjectId = "super123"
        
        superObjectController.delete(superObjectId)
        
        verify(superObjectService).delete(superObjectId)
    }
    
    @Test
    fun `syncDocumentBlocks - should sync document blocks and return updated super object`() {
        val superObjectId = "super123"
        val blocksPayload = listOf(
            EditorJsBlockDto(
                id = "block1",
                type = "paragraph",
                data = mapOf("text" to "Test paragraph")
            ),
            EditorJsBlockDto(
                id = "block2",
                type = "header",
                data = mapOf("text" to "Test header", "level" to 2)
            )
        )
        
        val updatedSuperObject = SuperObject(
            id = superObjectId,
            fileId = 1L,
            serviceType = "NOTE",
            name = "Test Super Object",
            firstItem = "block1",
            lastItem = "block2"
        )
        
        `when`(superObjectService.syncDocumentBlocks(superObjectId, blocksPayload)).thenReturn(updatedSuperObject)
        
        val result = superObjectController.syncDocumentBlocks(superObjectId, blocksPayload)
        
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(updatedSuperObject, result.body)
    }
    
    @Test
    fun `getById - when super object doesn't exist - should propagate exception`() {
        val superObjectId = "nonexistent"
        
        `when`(superObjectService.getById(superObjectId))
            .thenThrow(NoSuchElementException("Not found"))
        
        try {

            superObjectController.getById(superObjectId)
            fail("Should have thrown NoSuchElementException")
        } catch (e: NoSuchElementException) {

            assertEquals("Not found", e.message)
        }
    }
}
