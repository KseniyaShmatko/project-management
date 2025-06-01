package com.example.projectmanagement.controllers

import com.example.projectmanagement.services.FileDownloadService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class FileDownloadControllerTest {
    
    private lateinit var fileDownloadController: FileDownloadController
    private lateinit var fileDownloadService: FileDownloadService
    
    @BeforeEach
    fun setup() {
        fileDownloadService = mock(FileDownloadService::class.java)
        fileDownloadController = FileDownloadController(fileDownloadService)
    }
    
    @Test
    fun `downloadFile - should return file resource`() {
        val projectId = 1L
        val fileId = 2L
        val mockResource = mock(Resource::class.java)
        val filename = "test-file.txt"
        
        `when`(mockResource.filename).thenReturn(filename)
        `when`(fileDownloadService.getFileAsResource(projectId, fileId)).thenReturn(mockResource)
        
        val result = fileDownloadController.downloadFile(projectId, fileId)
        
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, result.headers.contentType)
        assertEquals("attachment; filename=\"$filename\"", result.headers.getFirst(HttpHeaders.CONTENT_DISPOSITION))
        assertEquals(mockResource, result.body)
        verify(fileDownloadService).getFileAsResource(projectId, fileId)
    }
    
    @Test
    fun `downloadFile - when file has no filename - should use default`() {
        val projectId = 1L
        val fileId = 2L
        val mockResource = mock(Resource::class.java)
        
        `when`(mockResource.filename).thenReturn(null)
        `when`(fileDownloadService.getFileAsResource(projectId, fileId)).thenReturn(mockResource)
        
        val result = fileDownloadController.downloadFile(projectId, fileId)
        
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals("attachment; filename=\"downloaded_file\"", result.headers.getFirst(HttpHeaders.CONTENT_DISPOSITION))
        assertEquals(mockResource, result.body)
    }
    
    @Test
    fun `downloadFile - when file not found - should propagate exception`() {
        val projectId = 1L
        val fileId = 999L
        
        `when`(fileDownloadService.getFileAsResource(projectId, fileId))
            .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in this project."))
        
        try {

            fileDownloadController.downloadFile(projectId, fileId)
            fail("Should have thrown ResponseStatusException")
        } catch (e: ResponseStatusException) {

            assertEquals(HttpStatus.NOT_FOUND, e.statusCode)
            assertEquals("File not found in this project.", e.reason)
        }
    }
}
