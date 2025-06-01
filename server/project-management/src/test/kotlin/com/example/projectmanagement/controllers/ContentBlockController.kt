package com.example.projectmanagement.controllers

import com.example.projectmanagement.models.mongo.ContentBlock
import com.example.projectmanagement.services.ContentBlockService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.NoSuchElementException
import kotlin.test.assertEquals
import kotlin.test.fail

class ContentBlockControllerTest {
    
    private lateinit var contentBlockController: ContentBlockController
    private lateinit var contentBlockService: ContentBlockService
    
    @BeforeEach
    fun setup() {
        contentBlockService = mock(ContentBlockService::class.java)
        contentBlockController = ContentBlockController(contentBlockService)
    }
    
    @Test
    fun `create - should create and return content block`() {
        val contentBlock = ContentBlock(
            objectType = "paragraph",
            data = mapOf("text" to "Test paragraph")
        )
        
        val createdBlock = ContentBlock(
            id = "block123",
            objectType = "paragraph",
            data = mapOf("text" to "Test paragraph")
        )
        
        `when`(contentBlockService.create(contentBlock)).thenReturn(createdBlock)
        
        val result = contentBlockController.create(contentBlock)
        
        assertEquals(createdBlock, result)
        verify(contentBlockService).create(contentBlock)
    }
    
    @Test
    fun `getById - should return content block by id`() {
        val blockId = "block123"
        val contentBlock = ContentBlock(
            id = blockId,
            objectType = "paragraph",
            data = mapOf("text" to "Test paragraph")
        )
        
        `when`(contentBlockService.getById(blockId)).thenReturn(contentBlock)
        
        val result = contentBlockController.getById(blockId)
        
        assertEquals(contentBlock, result)
        verify(contentBlockService).getById(blockId)
    }
    
    @Test
    fun `update - should update and return content block`() {
        val blockId = "block123"
        val contentBlock = ContentBlock(
            objectType = "paragraph",
            data = mapOf("text" to "Updated paragraph")
        )
        
        val updatedBlock = ContentBlock(
            id = blockId,
            objectType = "paragraph",
            data = mapOf("text" to "Updated paragraph")
        )
        
        `when`(contentBlockService.update(blockId, contentBlock)).thenReturn(updatedBlock)
        
        val result = contentBlockController.update(blockId, contentBlock)
        
        assertEquals(updatedBlock, result)
        verify(contentBlockService).update(blockId, contentBlock)
    }
    
    @Test
    fun `delete - should delete content block`() {
        val blockId = "block123"
        
        contentBlockController.delete(blockId)
        
        verify(contentBlockService).delete(blockId)
    }
    
    @Test
    fun `getById - when block doesn't exist - should propagate exception`() {
        val blockId = "nonexistent"
        
        `when`(contentBlockService.getById(blockId))
            .thenThrow(NoSuchElementException("ContentBlock with id '$blockId' not found"))
        
        try {

            contentBlockController.getById(blockId)
            fail("Should have thrown NoSuchElementException")
        } catch (e: NoSuchElementException) {

            assertEquals("ContentBlock with id '$blockId' not found", e.message)
        }
    }
    
    @Test
    fun `delete - when block doesn't exist - should propagate exception`() {
        val blockId = "nonexistent"
        
        doThrow(ResponseStatusException(HttpStatus.NOT_FOUND, "ContentBlock with id '$blockId' not found for deletion"))
            .`when`(contentBlockService).delete(blockId)
        
        try {

            contentBlockController.delete(blockId)
            fail("Should have thrown ResponseStatusException")
        } catch (e: ResponseStatusException) {

            assertEquals(HttpStatus.NOT_FOUND, e.statusCode)
            assertEquals("ContentBlock with id '$blockId' not found for deletion", e.reason)
        }
    }
}
