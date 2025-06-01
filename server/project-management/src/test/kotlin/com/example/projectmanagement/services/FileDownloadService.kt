package com.example.projectmanagement.services

import com.example.projectmanagement.models.File
import com.example.projectmanagement.models.FileType
import com.example.projectmanagement.models.Project
import com.example.projectmanagement.models.ProjectFile
import com.example.projectmanagement.models.ProjectRole
import com.example.projectmanagement.models.User
import com.example.projectmanagement.repositories.FileRepository
import com.example.projectmanagement.repositories.ProjectFileRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.*
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FileDownloadServiceTest {

    private lateinit var fileDownloadService: FileDownloadService
    private lateinit var projectService: ProjectService
    private lateinit var projectFileRepository: ProjectFileRepository
    private lateinit var fileRepository: FileRepository
    private lateinit var mockUser: User
    
    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setup() {
        // Очищаем контекст безопасности перед каждым тестом
        SecurityContextHolder.clearContext()
        
        // Создаем моки репозиториев и сервисов
        projectService = mock(ProjectService::class.java)
        projectFileRepository = mock(ProjectFileRepository::class.java)
        fileRepository = mock(FileRepository::class.java)
        
        // Создаем тестового пользователя
        mockUser = User(
            id = 1L,
            name = "Test",
            surname = "User",
            login = "testuser",
            passwordInternal = "password"
        )
        
        // Настраиваем SecurityContextHolder
        val authentication = mock(Authentication::class.java)
        `when`(authentication.principal).thenReturn(mockUser)
        `when`(authentication.isAuthenticated).thenReturn(true)
        
        val securityContext = mock(SecurityContext::class.java)
        `when`(securityContext.authentication).thenReturn(authentication)
        
        SecurityContextHolder.setContext(securityContext)
        
        // Создаем сервис для тестирования
        fileDownloadService = FileDownloadService(
            projectService,
            projectFileRepository,
            fileRepository
        )
    }

    @Test
    fun `getFileAsResource - when file exists - should return resource`() {
        // Arrange
        val projectId = 1L
        val fileId = 2L
        
        // Создаем тестовый файл во временной директории
        val testFilePath = tempDir.resolve("test.txt")
        Files.write(testFilePath, "Test content".toByteArray())
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description"
        )
        
        val fileType = FileType(
            id = 1L,
            name = "document"
        )
        
        val file = File(
            id = fileId,
            name = "Test File",
            type = fileType,
            authorId = mockUser.id,
            uploadDate = LocalDateTime.now(),
            filePath = testFilePath.toString()
        )
        
        val projectFile = ProjectFile(
            id = 1L,
            project = project,
            file = file
        )
        
        // Настраиваем моки
        doNothing().`when`(projectService).checkAccessToProject(projectId, mockUser.id, listOf(ProjectRole.VIEWER, ProjectRole.EDITOR, ProjectRole.OWNER))
        `when`(projectFileRepository.findByProject_IdAndFile_Id(projectId, fileId)).thenReturn(projectFile)
        `when`(fileRepository.findById(fileId)).thenReturn(Optional.of(file))
        
        // Act
        val result = fileDownloadService.getFileAsResource(projectId, fileId)
        
        // Assert
        assertTrue(result.exists())
        assertTrue(result.isReadable())
        
        verify(projectService).checkAccessToProject(projectId, mockUser.id, listOf(ProjectRole.VIEWER, ProjectRole.EDITOR, ProjectRole.OWNER))
        verify(projectFileRepository).findByProject_IdAndFile_Id(projectId, fileId)
        verify(fileRepository).findById(fileId)
    }

    @Test
    fun `getFileAsResource - when file not found in project - should throw not found exception`() {
        // Arrange
        val projectId = 1L
        val fileId = 2L
        
        // Настраиваем моки
        doNothing().`when`(projectService).checkAccessToProject(projectId, mockUser.id, listOf(ProjectRole.VIEWER, ProjectRole.EDITOR, ProjectRole.OWNER))
        `when`(projectFileRepository.findByProject_IdAndFile_Id(projectId, fileId)).thenReturn(null)
        
        // Act & Assert
        val exception = assertFailsWith<ResponseStatusException> {
            fileDownloadService.getFileAsResource(projectId, fileId)
        }
        
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertEquals("File not found in this project.", exception.reason)
        
        verify(projectService).checkAccessToProject(projectId, mockUser.id, listOf(ProjectRole.VIEWER, ProjectRole.EDITOR, ProjectRole.OWNER))
        verify(projectFileRepository).findByProject_IdAndFile_Id(projectId, fileId)
        verify(fileRepository, never()).findById(any())
    }

    @Test
    fun `getFileAsResource - when file path is null - should throw internal server error`() {
        // Arrange
        val projectId = 1L
        val fileId = 2L
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description"
        )
        
        val fileType = FileType(
            id = 1L,
            name = "document"
        )
        
        val file = File(
            id = fileId,
            name = "Test File",
            type = fileType,
            authorId = mockUser.id,
            uploadDate = LocalDateTime.now(),
            filePath = null // Отсутствует путь к файлу
        )
        
        val projectFile = ProjectFile(
            id = 1L,
            project = project,
            file = file
        )
        
        // Настраиваем моки
        doNothing().`when`(projectService).checkAccessToProject(projectId, mockUser.id, listOf(ProjectRole.VIEWER, ProjectRole.EDITOR, ProjectRole.OWNER))
        `when`(projectFileRepository.findByProject_IdAndFile_Id(projectId, fileId)).thenReturn(projectFile)
        `when`(fileRepository.findById(fileId)).thenReturn(Optional.of(file))
        
        // Act & Assert
        val exception = assertFailsWith<ResponseStatusException> {
            fileDownloadService.getFileAsResource(projectId, fileId)
        }
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.statusCode)
        assertEquals("File path is not set.", exception.reason)
        
        verify(projectService).checkAccessToProject(projectId, mockUser.id, listOf(ProjectRole.VIEWER, ProjectRole.EDITOR, ProjectRole.OWNER))
        verify(projectFileRepository).findByProject_IdAndFile_Id(projectId, fileId)
        verify(fileRepository).findById(fileId)
    }
}
