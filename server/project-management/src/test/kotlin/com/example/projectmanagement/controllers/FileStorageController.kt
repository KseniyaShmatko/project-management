package com.example.projectmanagement.controllers

import com.example.projectmanagement.controllers.dto.FileDetails
import com.example.projectmanagement.controllers.dto.FileUploadResponse
import com.example.projectmanagement.services.FileStorageService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import kotlin.test.assertEquals

class FileStorageControllerTest {
    
    private lateinit var fileStorageController: FileStorageController
    private lateinit var fileStorageService: FileStorageService
    
    @BeforeEach
    fun setup() {
        fileStorageService = mock(FileStorageService::class.java)
        fileStorageController = FileStorageController(fileStorageService)
    }
    
    @Test
    fun `uploadImage - should upload and return success response`() {
        val file = MockMultipartFile(
            "image", 
            "test.jpg", 
            "image/jpeg", 
            "test image content".toByteArray()
        )
        
        val expectedResponse = FileUploadResponse(
            success = 1,
            file = FileDetails(
                url = "http://localhost:8080/uploads/test.jpg",
                name = "test.jpg",
                size = file.size
            )
        )
        
        `when`(fileStorageService.storeFile(file)).thenReturn(expectedResponse)
        
        val result = fileStorageController.uploadImage(file)
        
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(expectedResponse, result.body)
        verify(fileStorageService).storeFile(file)
    }
    
    @Test
    fun `uploadImage - when error occurs - should return error response`() {
        val file = MockMultipartFile(
            "image", 
            "test.jpg", 
            "image/jpeg", 
            "test image content".toByteArray()
        )
        
        `when`(fileStorageService.storeFile(file)).thenThrow(RuntimeException("Storage error"))
        
        val result = fileStorageController.uploadImage(file)
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.statusCode)
        val responseBody = result.body
        assertEquals(0, responseBody?.success)
        assertEquals("", responseBody?.file?.url)
        assertEquals("test.jpg", responseBody?.file?.name)
    }
}
