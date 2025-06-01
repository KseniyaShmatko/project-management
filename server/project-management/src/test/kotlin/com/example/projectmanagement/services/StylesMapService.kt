package com.example.projectmanagement.services

import com.example.projectmanagement.models.mongo.StyleLink
import com.example.projectmanagement.models.mongo.StylesMap
import com.example.projectmanagement.repositories.mongo.StylesMapRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StylesMapServiceTest {

    private lateinit var stylesMapService: StylesMapService
    private lateinit var stylesMapRepository: StylesMapRepository

    @BeforeEach
    fun setup() {
        stylesMapRepository = mock(StylesMapRepository::class.java)
        stylesMapService = StylesMapService(stylesMapRepository)
    }

    @Test
    fun `create - should save and return styles map`() {
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
        
        val savedStylesMap = StylesMap(
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
        
        `when`(stylesMapRepository.save(stylesMap)).thenReturn(savedStylesMap)
        
        val result = stylesMapService.create(stylesMap)
        
        assertEquals(savedStylesMap, result)
        verify(stylesMapRepository).save(stylesMap)
    }

    @Test
    fun `getById - when styles map exists - should return styles map`() {
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
        
        `when`(stylesMapRepository.findById(mapId)).thenReturn(Optional.of(stylesMap))
        
        val result = stylesMapService.getById(mapId)
        
        assertEquals(stylesMap, result)
        verify(stylesMapRepository).findById(mapId)
    }

    @Test
    fun `getById - when styles map doesn't exist - should throw NoSuchElementException`() {
        val mapId = "nonexistent"
        
        `when`(stylesMapRepository.findById(mapId)).thenReturn(Optional.empty())
        
        val exception = assertFailsWith<NoSuchElementException> {
            stylesMapService.getById(mapId)
        }
        
        assertEquals("Not found", exception.message)
        verify(stylesMapRepository).findById(mapId)
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
        
        `when`(stylesMapRepository.save(any())).thenReturn(updatedStylesMap)
        
        val result = stylesMapService.update(mapId, stylesMap)
        
        assertEquals(updatedStylesMap, result)
        assertEquals(mapId, stylesMap.id)
        verify(stylesMapRepository).save(stylesMap)
    }

    @Test
    fun `delete - should delete styles map`() {
        val mapId = "map123"
        
        stylesMapService.delete(mapId)
        
        verify(stylesMapRepository).deleteById(mapId)
    }
}
