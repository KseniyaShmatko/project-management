package com.example.projectmanagement.controllers

import com.example.projectmanagement.models.mongo.StyleLink
import com.example.projectmanagement.models.mongo.StylesMap
import com.example.projectmanagement.services.StylesMapService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.NoSuchElementException
import kotlin.test.assertEquals
import kotlin.test.fail

class StylesMapControllerTest {
    
    private lateinit var stylesMapController: StylesMapController
    private lateinit var stylesMapService: StylesMapService
    
    @BeforeEach
    fun setup() {
        stylesMapService = mock(StylesMapService::class.java)
        stylesMapController = StylesMapController(stylesMapService)
    }
    
    @Test
    fun `create - should create and return styles map`() {
        val stylesMap = StylesMap(
            objectType = "styles_map",
            links = listOf(
                StyleLink(
                    elementId = "elem1",
                    styleId = "style1",
                    start = 0,
                    end = 10
                )
            )
        )
        
        val createdStylesMap = StylesMap(
            id = "map123",
            objectType = "styles_map",
            links = listOf(
                StyleLink(
                    elementId = "elem1",
                    styleId = "style1",
                    start = 0,
                    end = 10
                )
            )
        )
        
        `when`(stylesMapService.create(stylesMap)).thenReturn(createdStylesMap)
        
        val result = stylesMapController.create(stylesMap)
        
        assertEquals(createdStylesMap, result)
        verify(stylesMapService).create(stylesMap)
    }
    
    @Test
    fun `getById - should return styles map by id`() {
        val mapId = "map123"
        val stylesMap = StylesMap(
            id = mapId,
            objectType = "styles_map",
            links = listOf(
                StyleLink(
                    elementId = "elem1",
                    styleId = "style1",
                    start = 0,
                    end = 10
                )
            )
        )
        
        `when`(stylesMapService.getById(mapId)).thenReturn(stylesMap)
        
        val result = stylesMapController.getById(mapId)
        
        assertEquals(stylesMap, result)
        verify(stylesMapService).getById(mapId)
    }
    
    @Test
    fun `update - should update and return styles map`() {
        val mapId = "map123"
        val stylesMap = StylesMap(
            objectType = "styles_map",
            links = listOf(
                StyleLink(
                    elementId = "elem1",
                    styleId = "style2",
                    start = 5,
                    end = 15
                )
            )
        )
        
        val updatedStylesMap = StylesMap(
            id = mapId,
            objectType = "styles_map",
            links = listOf(
                StyleLink(
                    elementId = "elem1",
                    styleId = "style2",
                    start = 5,
                    end = 15
                )
            )
        )
        
        `when`(stylesMapService.update(mapId, stylesMap)).thenReturn(updatedStylesMap)
        
        val result = stylesMapController.update(mapId, stylesMap)
        
        assertEquals(updatedStylesMap, result)
        verify(stylesMapService).update(mapId, stylesMap)
    }
    
    @Test
    fun `delete - should delete styles map`() {
        val mapId = "map123"
        
        stylesMapController.delete(mapId)
        
        verify(stylesMapService).delete(mapId)
    }
    
    @Test
    fun `getById - when styles map doesn't exist - should propagate exception`() {
        val mapId = "nonexistent"
        
        `when`(stylesMapService.getById(mapId))
            .thenThrow(NoSuchElementException("Not found"))
        
        try {

            stylesMapController.getById(mapId)
            fail("Should have thrown NoSuchElementException")
        } catch (e: NoSuchElementException) {

            assertEquals("Not found", e.message)
        }
    }
}
