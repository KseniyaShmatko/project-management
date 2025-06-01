package com.example.projectmanagement.controllers

import com.example.projectmanagement.controllers.dto.*
import com.example.projectmanagement.models.*
import com.example.projectmanagement.services.ProjectFileService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class ProjectFileControllerTest {

    private lateinit var projectFileController: ProjectFileController
    private lateinit var projectFileService: ProjectFileService
    
    @BeforeEach
    fun setup() {
        projectFileService = mock(ProjectFileService::class.java)
        projectFileController = ProjectFileController(projectFileService)
    }
    
    @Test
    fun `uploadAndLinkFileToProject - should upload file and link to project`() {
        val projectId = 1L
        val typeId = 1L
        val multipartFile = MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "Test content".toByteArray()
        )
        
        val fileType = FileTypeResponseDto(id = typeId, name = "document")
        val responseDto = ProjectFileResponseDto(
            id = 1L,
            name = "test.txt",
            type = fileType,
            authorId = 1L,
            date = LocalDateTime.now().toString(),
            superObjectId = null
        )
        
        `when`(projectFileService.uploadAndLinkFile(projectId, multipartFile, typeId)).thenReturn(responseDto)
        
        val result = projectFileController.uploadAndLinkFileToProject(projectId, multipartFile, typeId)
        
        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(responseDto, result.body)
    }
    
    @Test
    fun `linkExistingFileToProject - should link existing file to project`() {
        val projectId = 1L
        val fileId = 2L
        
        val project = Project(id = projectId, name = "Test Project", description = null)
        val fileType = FileType(id = 1L, name = "document")
        val file = File(
            id = fileId,
            name = "Test File",
            type = fileType,
            authorId = 1L,
            uploadDate = LocalDateTime.now()
        )
        val projectFile = ProjectFile(
            id = 1L,
            project = project,
            file = file
        )
        
        `when`(projectFileService.linkExistingFile(projectId, fileId)).thenReturn(projectFile)
        
        val result = projectFileController.linkExistingFileToProject(projectId, fileId)
        
        assertEquals(HttpStatus.OK, result.statusCode)
        val responseMap = result.body as Map<*, *>
        assertEquals(projectId, responseMap["project_id"])
        assertEquals(fileId, responseMap["file_id"])
    }
    
    @Test
    fun `getFilesForProject - should return files for project`() {
        val projectId = 1L
        val fileType = FileTypeResponseDto(id = 1L, name = "document")
        
        val files = listOf(
            ProjectFileResponseDto(
                id = 1L,
                name = "File 1",
                type = fileType,
                authorId = 1L,
                date = LocalDateTime.now().toString(),
                superObjectId = null
            ),
            ProjectFileResponseDto(
                id = 2L,
                name = "File 2",
                type = fileType,
                authorId = 1L,
                date = LocalDateTime.now().toString(),
                superObjectId = null
            )
        )
        
        `when`(projectFileService.getFilesForProjectWithDetails(projectId)).thenReturn(files)
        
        val result = projectFileController.getFilesForProject(projectId)
        
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(files, result.body)
    }
    
    @Test
    fun `getFileInProject - should return file details in project`() {
        val projectId = 1L
        val fileId = 2L
        val fileType = FileTypeResponseDto(id = 1L, name = "document")
        
        val fileDto = ProjectFileResponseDto(
            id = fileId,
            name = "Test File",
            type = fileType,
            authorId = 1L,
            date = LocalDateTime.now().toString(),
            superObjectId = null
        )
        
        `when`(projectFileService.getFileDetailsInProject(projectId, fileId)).thenReturn(fileDto)
        
        val result = projectFileController.getFileInProject(projectId, fileId)
        
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(fileDto, result.body)
    }
    
    @Test
    fun `updateFileNameInProject - should update file name in project`() {
        val projectId = 1L
        val fileId = 2L
        val newName = "Updated File Name"
        val updateDto = FileUpdateNameDto(name = newName)
        
        val fileType = FileTypeResponseDto(id = 1L, name = "document")
        val updatedFileDto = ProjectFileResponseDto(
            id = fileId,
            name = newName,
            type = fileType,
            authorId = 1L,
            date = LocalDateTime.now().toString(),
            superObjectId = null
        )
        
        `when`(projectFileService.updateFileNameInProject(projectId, fileId, newName))
            .thenReturn(updatedFileDto)
        
        val result = projectFileController.updateFileNameInProject(projectId, fileId, updateDto)
        
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(updatedFileDto, result.body)
    }
    
    @Test
    fun `updateSuperObjectIdInProject - should update super object id in project`() {
        val projectId = 1L
        val fileId = 2L
        val superObjectId = "super123"
        val request = mapOf("superObjectId" to superObjectId)
        
        val fileType = FileTypeResponseDto(id = 1L, name = "document")
        val updatedFileDto = ProjectFileResponseDto(
            id = fileId,
            name = "Test File",
            type = fileType,
            authorId = 1L,
            date = LocalDateTime.now().toString(),
            superObjectId = superObjectId
        )
        
        `when`(projectFileService.updateSuperObjectIdInProject(projectId, fileId, superObjectId))
            .thenReturn(updatedFileDto)
        
        val result = projectFileController.updateSuperObjectIdInProject(projectId, fileId, request)
        
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(updatedFileDto, result.body)
    }
    
    @Test
    fun `unlinkFileFromProject - should unlink file from project`() {
        val projectId = 1L
        val fileId = 2L
        
        val result = projectFileController.unlinkFileFromProject(projectId, fileId)
        
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
        verify(projectFileService).unlinkFile(projectId, fileId)
    }
    
    @Test
    fun `getFilesForProject - when project doesn't exist - should propagate exception`() {
        val projectId = 999L
        `when`(projectFileService.getFilesForProjectWithDetails(projectId))
            .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"))
        
        try {

            projectFileController.getFilesForProject(projectId)
            fail("Should have thrown ResponseStatusException")
        } catch (e: ResponseStatusException) {

            assertEquals(HttpStatus.NOT_FOUND, e.statusCode)
            assertEquals("Project not found", e.reason)
        }
    }
}
