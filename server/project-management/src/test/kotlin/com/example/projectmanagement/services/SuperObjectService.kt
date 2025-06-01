package com.example.projectmanagement.services

import com.example.projectmanagement.controllers.dto.EditorJsBlockDto
import com.example.projectmanagement.models.mongo.ContentBlock
import com.example.projectmanagement.models.mongo.SuperObject
import com.example.projectmanagement.models.mongo.Template
import com.example.projectmanagement.repositories.mongo.ContentBlockRepository
import com.example.projectmanagement.repositories.mongo.SuperObjectRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.dao.DuplicateKeyException
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class SuperObjectServiceTest {

    private lateinit var superObjectService: SuperObjectService
    private lateinit var superObjectRepository: SuperObjectRepository
    private lateinit var contentBlockRepository: ContentBlockRepository

    @BeforeEach
    fun setup() {
        superObjectRepository = mock(SuperObjectRepository::class.java)
        contentBlockRepository = mock(ContentBlockRepository::class.java)
        superObjectService = SuperObjectService(superObjectRepository, contentBlockRepository)
    }

    @Test
    fun `create - when fileId is provided - should save and return super object`() {
        // Arrange
        val superObject = SuperObject(
            fileId = 1L,
            serviceType = "NOTE",
            name = "Test Super Object"
        )

        val savedSuperObject = SuperObject(
            id = "super123",
            fileId = 1L,
            serviceType = "NOTE",
            name = "Test Super Object"
        )

        `when`(superObjectRepository.save(any(SuperObject::class.java))).thenReturn(savedSuperObject)

        // Act
        val result = superObjectService.create(superObject)

        // Assert
        assertNotNull(result)
        assertEquals(savedSuperObject.id, result.id)
        assertEquals(savedSuperObject.fileId, result.fileId)
        assertEquals(savedSuperObject.serviceType, result.serviceType)
        assertEquals(savedSuperObject.name, result.name)

        verify(superObjectRepository).save(any(SuperObject::class.java))
    }

    @Test
    fun `create - when fileId is null - should throw IllegalArgumentException`() {
        // Arrange
        val superObject = SuperObject(
            fileId = null,
            serviceType = "NOTE",
            name = "Test Super Object"
        )

        // Act & Assert
        val exception = assertFailsWith<IllegalArgumentException> {
            superObjectService.create(superObject)
        }

        assertEquals("fileId must be provided to create SuperObject", exception.message)
        verify(superObjectRepository, never()).save(any(SuperObject::class.java))
    }

    @Test
    fun `create - when duplicate key exception - should return existing super object`() {
        // Arrange
        val superObject = SuperObject(
            fileId = 1L,
            serviceType = "NOTE",
            name = "Test Super Object"
        )

        val existingSuperObject = SuperObject(
            id = "super123",
            fileId = 1L,
            serviceType = "NOTE",
            name = "Existing Super Object"
        )

        `when`(superObjectRepository.save(any(SuperObject::class.java))).thenThrow(DuplicateKeyException("Duplicate key"))
        `when`(superObjectRepository.findByFileId(1L)).thenReturn(existingSuperObject)

        // Act
        val result = superObjectService.create(superObject)

        // Assert
        assertNotNull(result)
        assertEquals(existingSuperObject.id, result.id)
        assertEquals(existingSuperObject.fileId, result.fileId)
        assertEquals(existingSuperObject.name, result.name)

        verify(superObjectRepository).save(any(SuperObject::class.java))
        verify(superObjectRepository).findByFileId(1L)
    }

    @Test
    fun `getByFileId - when super object exists - should return super object`() {
        // Arrange
        val fileId = 1L
        val superObject = SuperObject(
            id = "super123",
            fileId = fileId,
            serviceType = "NOTE",
            name = "Test Super Object"
        )

        `when`(superObjectRepository.findByFileId(fileId)).thenReturn(superObject)

        // Act
        val result = superObjectService.getByFileId(fileId)

        // Assert
        assertNotNull(result)
        assertEquals(superObject.id, result?.id)
        assertEquals(superObject.fileId, result?.fileId)
        assertEquals(superObject.name, result?.name)

        verify(superObjectRepository).findByFileId(fileId)
    }

    @Test
    fun `getById - when super object exists - should return super object`() {
        // Arrange
        val superObjectId = "super123"
        val superObject = SuperObject(
            id = superObjectId,
            fileId = 1L,
            serviceType = "NOTE",
            name = "Test Super Object"
        )

        `when`(superObjectRepository.findById(superObjectId)).thenReturn(Optional.of(superObject))

        // Act
        val result = superObjectService.getById(superObjectId)

        // Assert
        assertNotNull(result)
        assertEquals(superObject.id, result.id)
        assertEquals(superObject.fileId, result.fileId)
        assertEquals(superObject.name, result.name)

        verify(superObjectRepository).findById(superObjectId)
    }

    @Test
    fun `getById - when super object doesn't exist - should throw NoSuchElementException`() {
        // Arrange
        val superObjectId = "nonexistent"

        `when`(superObjectRepository.findById(superObjectId)).thenReturn(Optional.empty())

        // Act & Assert
        val exception = assertFailsWith<NoSuchElementException> {
            superObjectService.getById(superObjectId)
        }

        assertEquals("Not found", exception.message)
        verify(superObjectRepository).findById(superObjectId)
    }

    @Test
    fun `update - when super object exists - should update and return super object`() {
        // Arrange
        val superObjectId = "super123"
        val existingSuperObject = SuperObject(
            id = superObjectId,
            fileId = 1L,
            serviceType = "NOTE",
            name = "Original Super Object"
        )

        val updates = SuperObject(
            name = "Updated Super Object",
            serviceType = "DOCUMENT"
        )

        val updatedSuperObject = SuperObject(
            id = superObjectId,
            fileId = 1L,
            serviceType = "DOCUMENT",
            name = "Updated Super Object"
        )

        `when`(superObjectRepository.findById(superObjectId)).thenReturn(Optional.of(existingSuperObject))
        `when`(superObjectRepository.save(any(SuperObject::class.java))).thenReturn(updatedSuperObject)

        // Act
        val result = superObjectService.update(superObjectId, updates)

        // Assert
        assertNotNull(result)
        assertEquals(updatedSuperObject.id, result.id)
        assertEquals(updatedSuperObject.fileId, result.fileId)
        assertEquals(updatedSuperObject.serviceType, result.serviceType)
        assertEquals(updatedSuperObject.name, result.name)

        verify(superObjectRepository).findById(superObjectId)
        verify(superObjectRepository).save(any(SuperObject::class.java))
    }

    @Test
    fun `syncDocumentBlocks - should sync blocks and update super object`() {
        // Arrange
        val superObjectId = "super123"
        val superObject = SuperObject(
            id = superObjectId,
            fileId = 1L,
            serviceType = "NOTE",
            name = "Test Super Object",
            firstItem = "block1",
            lastItem = "block2"
        )

        val existingBlock1 = ContentBlock(
            id = "block1",
            objectType = "paragraph",
            data = mapOf("text" to "Paragraph 1"),
            nextItem = "block2"
        )

        val existingBlock2 = ContentBlock(
            id = "block2",
            objectType = "paragraph",
            data = mapOf("text" to "Paragraph 2"),
            prevItem = "block1"
        )

        val editorJsBlocks = listOf(
            EditorJsBlockDto(
                id = "block1",
                type = "paragraph",
                data = mapOf("text" to "Updated Paragraph 1")
            ),
            EditorJsBlockDto(
                id = "block3", // New block
                type = "header",
                data = mapOf("text" to "New Header", "level" to 2)
            )
        )

        val updatedBlock1 = ContentBlock(
            id = "block1",
            objectType = "paragraph",
            data = mapOf("text" to "Updated Paragraph 1"),
            nextItem = "block3"
        )

        val savedNewBlock = ContentBlock(
            id = "block3",
            objectType = "header",
            data = mapOf("text" to "New Header", "level" to 2),
            prevItem = "block1"
        )

        val updatedSuperObject = SuperObject(
            id = superObjectId,
            fileId = 1L,
            serviceType = "NOTE",
            name = "Test Super Object",
            firstItem = "block1",
            lastItem = "block3"
        )

        `when`(superObjectRepository.findById(superObjectId)).thenReturn(Optional.of(superObject))
        `when`(contentBlockRepository.findById("block1")).thenReturn(Optional.of(existingBlock1))
        `when`(contentBlockRepository.findById("block2")).thenReturn(Optional.of(existingBlock2))
        `when`(contentBlockRepository.existsById("block1")).thenReturn(true)
        `when`(contentBlockRepository.existsById("block3")).thenReturn(false)
        `when`(contentBlockRepository.save(any(ContentBlock::class.java))).thenAnswer { invocation ->
            val block = invocation.getArgument<ContentBlock>(0)
            when (block.id) {
                "block1" -> updatedBlock1
                "block3" -> savedNewBlock
                else -> block
            }
        }
        `when`(superObjectRepository.save(any(SuperObject::class.java))).thenReturn(updatedSuperObject)

        // Act
        val result = superObjectService.syncDocumentBlocks(superObjectId, editorJsBlocks)

        // Assert
        assertNotNull(result)
        assertEquals(updatedSuperObject.id, result.id)
        assertEquals(updatedSuperObject.firstItem, result.firstItem)
        assertEquals(updatedSuperObject.lastItem, result.lastItem)

        verify(superObjectRepository).findById(superObjectId)
        verify(contentBlockRepository, atLeastOnce()).save(any(ContentBlock::class.java))
        verify(superObjectRepository).save(any(SuperObject::class.java))
    }

    @Test
    fun `delete - should delete super object`() {
        // Arrange
        val superObjectId = "super123"

        // Act
        superObjectService.delete(superObjectId)

        // Assert
        verify(superObjectRepository).deleteById(superObjectId)
    }

    @Test
    fun `updateMetadata - when super object exists - should update metadata and return super object`() {
        // Arrange
        val superObjectId = "super123"
        val existingSuperObject = SuperObject(
            id = superObjectId,
            fileId = 1L,
            serviceType = "NOTE",
            name = "Original Super Object"
        )

        val template = Template(
            type = "custom",
            color = "#FF0000"
        )

        val updates = SuperObject(
            name = "Updated Super Object"
        )
        updates.template = template

        val updatedSuperObject = SuperObject(
            id = superObjectId,
            fileId = 1L,
            serviceType = "NOTE",
            name = "Updated Super Object"
        )
        updatedSuperObject.template = template

        `when`(superObjectRepository.findById(superObjectId)).thenReturn(Optional.of(existingSuperObject))
        `when`(superObjectRepository.save(any(SuperObject::class.java))).thenReturn(updatedSuperObject)

        // Act
        val result = superObjectService.updateMetadata(superObjectId, updates)

        // Assert
        assertNotNull(result)
        assertEquals(updatedSuperObject.id, result.id)
        assertEquals(updatedSuperObject.name, result.name)
        assertEquals(updatedSuperObject.template?.type, result.template?.type)
        assertEquals(updatedSuperObject.template?.color, result.template?.color)

        verify(superObjectRepository).findById(superObjectId)
        verify(superObjectRepository).save(any(SuperObject::class.java))
    }
}
