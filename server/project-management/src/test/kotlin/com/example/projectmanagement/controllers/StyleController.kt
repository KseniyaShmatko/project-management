package com.example.projectmanagement.controllers

import com.example.projectmanagement.models.mongo.Style
import com.example.projectmanagement.services.StyleService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.NoSuchElementException
import kotlin.test.assertEquals
import kotlin.test.fail

class StyleControllerTest {
    
    private lateinit var styleController: StyleController
    private lateinit var styleService: StyleService
    
    @BeforeEach
    fun setup() {
        styleService = mock(StyleService::class.java)
        styleController = StyleController(styleService)
    }
    
    @Test
    fun `create - should create and return style`() {
        val style = Style(
            objectType = "style",
            appliedObject = "text",
            color = "#FF0000",
            fontSize = 16
        )
        
        val createdStyle = Style(
            id = "style123",
            objectType = "style",
            appliedObject = "text",
            color = "#FF0000",
            fontSize = 16
        )
        
        `when`(styleService.create(style)).thenReturn(createdStyle)
        
        val result = styleController.create(style)
        
        assertEquals(createdStyle, result)
        verify(styleService).create(style)
    }
    
    @Test
    fun `getById - should return style by id`() {
        val styleId = "style123"
        val style = Style(
            id = styleId,
            objectType = "style",
            appliedObject = "text",
            color = "#FF0000",
            fontSize = 16
        )
        
        `when`(styleService.getById(styleId)).thenReturn(style)
        
        val result = styleController.getById(styleId)
        
        assertEquals(style, result)
        verify(styleService).getById(styleId)
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
        
        `when`(styleService.update(styleId, style)).thenReturn(updatedStyle)
        
        val result = styleController.update(styleId, style)
        
        assertEquals(updatedStyle, result)
        verify(styleService).update(styleId, style)
    }
    
    @Test
    fun `delete - should delete style`() {
        val styleId = "style123"
        
        styleController.delete(styleId)
        
        verify(styleService).delete(styleId)
    }
    
    @Test
    fun `getById - when style doesn't exist - should propagate exception`() {
        val styleId = "nonexistent"
        
        `when`(styleService.getById(styleId))
            .thenThrow(NoSuchElementException("Not found"))
        
        try {

            styleController.getById(styleId)
            fail("Should have thrown NoSuchElementException")
        } catch (e: NoSuchElementException) {

            assertEquals("Not found", e.message)
        }
    }
}
