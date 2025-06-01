package com.example.projectmanagement.services

import com.example.projectmanagement.models.mongo.ContentBlock
import com.example.projectmanagement.repositories.mongo.ContentBlockRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class ContentBlockServiceTest {
    
    private lateinit var contentBlockService: ContentBlockService
    private lateinit var contentBlockRepository: ContentBlockRepository
    
    @BeforeEach
    fun setup() {
        contentBlockRepository = mock(ContentBlockRepository::class.java)
        contentBlockService = ContentBlockService(contentBlockRepository)
    }
    
    @Test
    fun `create - when id is null - should save and return content block`() {

        val contentBlock = ContentBlock(
            id = null,
            objectType = "paragraph",
            data = mapOf("text" to "Test paragraph")
        )
        
        val savedContentBlock = ContentBlock(
            id = "block123",
            objectType = "paragraph",
            data = mapOf("text" to "Test paragraph")
        )
        
        `when`(contentBlockRepository.save(contentBlock)).thenReturn(savedContentBlock)
        

        val result = contentBlockService.create(contentBlock)
        
        assertSame(savedContentBlock, result)
        verify(contentBlockRepository).save(contentBlock)
    }
    
    @Test
    fun `create - when id exists but not in repository - should save and return content block`() {

        val contentBlock = ContentBlock(
            id = "block123",
            objectType = "paragraph",
            data = mapOf("text" to "Test paragraph")
        )
        
        `when`(contentBlockRepository.existsById("block123")).thenReturn(false)
        `when`(contentBlockRepository.save(contentBlock)).thenReturn(contentBlock)
        

        val result = contentBlockService.create(contentBlock)
        
        assertSame(contentBlock, result)
        verify(contentBlockRepository).existsById("block123")
        verify(contentBlockRepository).save(contentBlock)
    }
    
    @Test
    fun `create - when id exists in repository - should throw conflict exception`() {

        val contentBlock = ContentBlock(
            id = "block123",
            objectType = "paragraph",
            data = mapOf("text" to "Test paragraph")
        )
        
        `when`(contentBlockRepository.existsById("block123")).thenReturn(true)
        
        val exception = assertFailsWith<ResponseStatusException> {
            contentBlockService.create(contentBlock)
        }
        
        assertEquals(HttpStatus.CONFLICT, exception.statusCode)
        verify(contentBlockRepository).existsById("block123")
        verify(contentBlockRepository, never()).save(any())
    }
    
    @Test
    fun `getById - when block exists - should return content block`() {

        val blockId = "block123"
        val contentBlock = ContentBlock(
            id = blockId,
            objectType = "paragraph",
            data = mapOf("text" to "Test paragraph")
        )
        
        `when`(contentBlockRepository.findById(blockId)).thenReturn(Optional.of(contentBlock))
        

        val result = contentBlockService.getById(blockId)
        
        assertSame(contentBlock, result)
        verify(contentBlockRepository).findById(blockId)
    }
    
    @Test
    fun `getById - when block doesn't exist - should throw NoSuchElementException`() {

        val blockId = "nonexistent"
        
        `when`(contentBlockRepository.findById(blockId)).thenReturn(Optional.empty())
        
        val exception = assertFailsWith<NoSuchElementException> {
            contentBlockService.getById(blockId)
        }
        
        assertEquals("ContentBlock with id '$blockId' not found", exception.message)
        verify(contentBlockRepository).findById(blockId)
    }
    
    @Test
    fun `update - when block exists - should update and return content block`() {

        val blockId = "block123"
        val existingBlock = ContentBlock(
            id = blockId,
            objectType = "paragraph",
            data = mapOf("text" to "Original paragraph")
        )
        
        val updates = ContentBlock(
            objectType = "header",
            data = mapOf("text" to "Updated paragraph", "level" to 1)
        )
        
        val updatedBlock = ContentBlock(
            id = blockId,
            objectType = "header",
            data = mapOf("text" to "Updated paragraph", "level" to 1)
        )
        
        `when`(contentBlockRepository.findById(blockId)).thenReturn(Optional.of(existingBlock))
        `when`(contentBlockRepository.save(any())).thenReturn(updatedBlock)
        

        val result = contentBlockService.update(blockId, updates)
        
        assertEquals(updatedBlock, result)
        verify(contentBlockRepository).findById(blockId)
        verify(contentBlockRepository).save(any())
    }
    
    @Test
    fun `update - when block doesn't exist - should throw NoSuchElementException`() {

        val blockId = "nonexistent"
        val updates = ContentBlock(
            objectType = "header",
            data = mapOf("text" to "Updated paragraph", "level" to 1)
        )
        
        `when`(contentBlockRepository.findById(blockId)).thenReturn(Optional.empty())
        
        val exception = assertFailsWith<NoSuchElementException> {
            contentBlockService.update(blockId, updates)
        }
        
        assertEquals("ContentBlock with id '$blockId' not found for update", exception.message)
        verify(contentBlockRepository).findById(blockId)
        verify(contentBlockRepository, never()).save(any())
    }
    
    @Test
    fun `delete - when block exists - should delete block`() {

        val blockId = "block123"
        
        `when`(contentBlockRepository.existsById(blockId)).thenReturn(true)
        

        contentBlockService.delete(blockId)
        
        verify(contentBlockRepository).existsById(blockId)
        verify(contentBlockRepository).deleteById(blockId)
    }
    
    @Test
    fun `delete - when block doesn't exist - should throw not found exception`() {

        val blockId = "nonexistent"
        
        `when`(contentBlockRepository.existsById(blockId)).thenReturn(false)
        
        val exception = assertFailsWith<ResponseStatusException> {
            contentBlockService.delete(blockId)
        }
        
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertEquals("ContentBlock with id '$blockId' not found for deletion", exception.reason)
        verify(contentBlockRepository).existsById(blockId)
        verify(contentBlockRepository, never()).deleteById(any())
    }
}
