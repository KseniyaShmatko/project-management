package com.example.projectmanagement.services

import com.example.projectmanagement.models.mongo.Style
import com.example.projectmanagement.repositories.mongo.StyleRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StyleServiceTest {

    private lateinit var styleService: StyleService
    private lateinit var styleRepository: StyleRepository

    @BeforeEach
    fun setup() {
        styleRepository = mock(StyleRepository::class.java)
        styleService = StyleService(styleRepository)
    }

    @Test
    fun `create - should save and return style`() {
        val style = Style(
            objectType = "style",
            appliedObject = "text",
            color = "#FF0000",
            fontSize = 16
        )
        
        val savedStyle = Style(
            id = "style123",
            objectType = "style",
            appliedObject = "text",
            color = "#FF0000",
            fontSize = 16
        )
        
        `when`(styleRepository.save(style)).thenReturn(savedStyle)
        
        val result = styleService.create(style)
        
        assertEquals(savedStyle, result)
        verify(styleRepository).save(style)
    }

    @Test
    fun `getById - when style exists - should return style`() {
        val styleId = "style123"
        val style = Style(
            id = styleId,
            objectType = "style",
            appliedObject = "text",
            color = "#FF0000",
            fontSize = 16
        )
        
        `when`(styleRepository.findById(styleId)).thenReturn(Optional.of(style))
        
        val result = styleService.getById(styleId)
        
        assertEquals(style, result)
        verify(styleRepository).findById(styleId)
    }

    @Test
    fun `getById - when style doesn't exist - should throw NoSuchElementException`() {
        val styleId = "nonexistent"
        
        `when`(styleRepository.findById(styleId)).thenReturn(Optional.empty())

        val exception = assertFailsWith<NoSuchElementException> {
            styleService.getById(styleId)
        }
        
        assertEquals("Not found", exception.message)
        verify(styleRepository).findById(styleId)
    }

    @Test
    fun `update - should update and return style`() {
        val styleId = "style123"
        val style = Style(
            objectType = "style",
            appliedObject = "text",
            color = "#00FF00",
            fontSize = 18
        )
        
        val updatedStyle = Style(
            id = styleId,
            objectType = "style",
            appliedObject = "text",
            color = "#00FF00",
            fontSize = 18
        )
        
        `when`(styleRepository.save(any())).thenReturn(updatedStyle)
        
        val result = styleService.update(styleId, style)
        
        assertEquals(updatedStyle, result)
        assertEquals(styleId, style.id)
        verify(styleRepository).save(style)
    }

    @Test
    fun `delete - should delete style`() {
        val styleId = "style123"
        
        styleService.delete(styleId)
        
        verify(styleRepository).deleteById(styleId)
    }
}
