package com.example.projectmanagement.services

import com.example.projectmanagement.controllers.dto.FileTypeResponseDto
import com.example.projectmanagement.controllers.dto.ProjectFileResponseDto
import com.example.projectmanagement.models.File
import com.example.projectmanagement.models.FileType
import com.example.projectmanagement.models.Project
import com.example.projectmanagement.models.ProjectFile
import com.example.projectmanagement.models.ProjectRole
import com.example.projectmanagement.models.User
import com.example.projectmanagement.repositories.FileRepository
import com.example.projectmanagement.repositories.FileTypeRepository
import com.example.projectmanagement.repositories.ProjectFileRepository
import com.example.projectmanagement.repositories.ProjectRepository
import com.example.projectmanagement.repositories.UserRepository
import com.example.projectmanagement.repositories.mongo.SuperObjectRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ProjectFileServiceTest {

    private lateinit var projectFileService: ProjectFileService
    private lateinit var projectFileRepository: ProjectFileRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var fileRepository: FileRepository
    private lateinit var fileTypeRepository: FileTypeRepository
    private lateinit var userRepository: UserRepository
    private lateinit var projectService: ProjectService
    private lateinit var superObjectRepository: SuperObjectRepository
    private lateinit var mockUser: User

    @BeforeEach
    fun setup() {
        // Очищаем контекст безопасности перед каждым тестом
        SecurityContextHolder.clearContext()
        
        // Создаем моки репозиториев и сервисов
        projectFileRepository = mock(ProjectFileRepository::class.java)
        projectRepository = mock(ProjectRepository::class.java)
        fileRepository = mock(FileRepository::class.java)
        fileTypeRepository = mock(FileTypeRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        projectService = mock(ProjectService::class.java)
        superObjectRepository = mock(SuperObjectRepository::class.java)
        
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
        projectFileService = ProjectFileService(
            projectFileRepository,
            projectRepository,
            fileRepository,
            fileTypeRepository,
            userRepository,
            projectService,
            superObjectRepository
        )
    }

    @Test
    fun `uploadAndLinkFile - should upload file and link to project`() {
        // Arrange
        val projectId = 1L
        val typeId = 1L
        val multipartFile = MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "Test content".toByteArray()
        )
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description"
        )
        
        val fileType = FileType(
            id = typeId,
            name = "document"
        )
        
        val savedFile = File(
            id = 1L,
            name = "test.txt",
            type = fileType,
            authorId = mockUser.id,
            uploadDate = LocalDateTime.now()
        )
        
        val projectFile = ProjectFile(
            id = 1L,
            project = project,
            file = savedFile
        )
        
        // Подготавливаем ожидаемый DTO ответа
        val expectedResponseDto = ProjectFileResponseDto(
            id = savedFile.id,
            name = savedFile.name,
            type = FileTypeResponseDto(id = fileType.id, name = fileType.name),
            authorId = savedFile.authorId,
            date = savedFile.uploadDate.toString(),
            superObjectId = null
        )
        
        // Настраиваем моки
        doNothing().`when`(projectService).checkAccessToProject(eq(projectId), eq(mockUser.id), anyList())
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(fileTypeRepository.findById(typeId)).thenReturn(Optional.of(fileType))
        `when`(fileRepository.save(any(File::class.java))).thenReturn(savedFile)
        `when`(projectFileRepository.save(any(ProjectFile::class.java))).thenReturn(projectFile)
        
        // Act
        val result = projectFileService.uploadAndLinkFile(projectId, multipartFile, typeId)
        
        // Assert
        assertNotNull(result)
        assertEquals(expectedResponseDto.id, result.id)
        assertEquals(expectedResponseDto.name, result.name)
        assertEquals(expectedResponseDto.type.id, result.type.id)
        assertEquals(expectedResponseDto.type.name, result.type.name)
        assertEquals(expectedResponseDto.authorId, result.authorId)
        
        verify(projectService).checkAccessToProject(eq(projectId), eq(mockUser.id), anyList())
        verify(projectRepository).findById(projectId)
        verify(fileTypeRepository).findById(typeId)
        verify(fileRepository).save(any(File::class.java))
        verify(projectFileRepository).save(any(ProjectFile::class.java))
    }

    @Test
    fun `linkExistingFile - should link existing file to project`() {
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
            name = "Existing File",
            type = fileType,
            authorId = mockUser.id,
            uploadDate = LocalDateTime.now()
        )
        
        val projectFile = ProjectFile(
            id = 1L,
            project = project,
            file = file
        )
        
        // Настраиваем моки
        doNothing().`when`(projectService).checkAccessToProject(eq(projectId), eq(mockUser.id), anyList())
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(fileRepository.findById(fileId)).thenReturn(Optional.of(file))
        `when`(projectFileRepository.existsByProject_IdAndFile_Id(projectId, fileId)).thenReturn(false)
        `when`(projectFileRepository.save(any(ProjectFile::class.java))).thenReturn(projectFile)
        
        // Act
        val result = projectFileService.linkExistingFile(projectId, fileId)
        
        // Assert
        assertNotNull(result)
        assertEquals(projectFile.id, result.id)
        assertEquals(project.id, result.project.id)
        assertEquals(file.id, result.file.id)
        
        verify(projectService).checkAccessToProject(eq(projectId), eq(mockUser.id), anyList())
        verify(projectRepository).findById(projectId)
        verify(fileRepository).findById(fileId)
        verify(projectFileRepository).existsByProject_IdAndFile_Id(projectId, fileId)
        verify(projectFileRepository).save(any(ProjectFile::class.java))
    }

    @Test
    fun `linkExistingFile - when file already linked - should throw conflict exception`() {
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
            name = "Existing File",
            type = fileType,
            authorId = mockUser.id,
            uploadDate = LocalDateTime.now()
        )
        
        // Настраиваем моки
        doNothing().`when`(projectService).checkAccessToProject(eq(projectId), eq(mockUser.id), anyList())
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(fileRepository.findById(fileId)).thenReturn(Optional.of(file))
        `when`(projectFileRepository.existsByProject_IdAndFile_Id(projectId, fileId)).thenReturn(true)
        
        // Act & Assert
        val exception = assertFailsWith<ResponseStatusException> {
            projectFileService.linkExistingFile(projectId, fileId)
        }
        
        assertEquals(HttpStatus.CONFLICT, exception.statusCode)
        assertEquals("File is already linked to this project.", exception.reason)
        
        verify(projectService).checkAccessToProject(eq(projectId), eq(mockUser.id), anyList())
        verify(projectRepository).findById(projectId)
        verify(fileRepository).findById(fileId)
        verify(projectFileRepository).existsByProject_IdAndFile_Id(projectId, fileId)
        verify(projectFileRepository, never()).save(any(ProjectFile::class.java))
    }

    @Test
    fun `getFilesForProjectWithDetails - should return files for project`() {
        // Arrange
        val projectId = 1L
        
        val fileType = FileType(
            id = 1L,
            name = "document"
        )
        
        val file1 = File(
            id = 1L,
            name = "File 1",
            type = fileType,
            authorId = mockUser.id,
            uploadDate = LocalDateTime.now()
        )
        
        val file2 = File(
            id = 2L,
            name = "File 2",
            type = fileType,
            authorId = mockUser.id,
            uploadDate = LocalDateTime.now()
        )
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description"
        )
        
        val projectFile1 = ProjectFile(
            id = 1L,
            project = project,
            file = file1
        )
        
        val projectFile2 = ProjectFile(
            id = 2L,
            project = project,
            file = file2
        )
        
        val projectFiles = listOf(projectFile1, projectFile2)
        
        // Подготавливаем ожидаемые DTO ответа
        val expectedFileResponses = listOf(
            ProjectFileResponseDto(
                id = file1.id,
                name = file1.name,
                type = FileTypeResponseDto(id = fileType.id, name = fileType.name),
                authorId = file1.authorId,
                date = file1.uploadDate.toString(),
                superObjectId = null
            ),
            ProjectFileResponseDto(
                id = file2.id,
                name = file2.name,
                type = FileTypeResponseDto(id = fileType.id, name = fileType.name),
                authorId = file2.authorId,
                date = file2.uploadDate.toString(),
                superObjectId = null
            )
        )
        
        // Настраиваем моки
        doNothing().`when`(projectService).checkAccessToProject(eq(projectId), eq(mockUser.id), anyList())
        `when`(projectFileRepository.findAllByProject_Id(projectId)).thenReturn(projectFiles)
        
        // Act
        val result = projectFileService.getFilesForProjectWithDetails(projectId)
        
        // Assert
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals(expectedFileResponses[0].id, result[0].id)
        assertEquals(expectedFileResponses[0].name, result[0].name)
        assertEquals(expectedFileResponses[1].id, result[1].id)
        assertEquals(expectedFileResponses[1].name, result[1].name)
        
        verify(projectService).checkAccessToProject(eq(projectId), eq(mockUser.id), anyList())
        verify(projectFileRepository).findAllByProject_Id(projectId)
    }

    @Test
    fun `updateFileNameInProject - should update file name`() {
        // Arrange
        val projectId = 1L
        val fileId = 2L
        val newName = "Updated File Name"
        
        val fileType = FileType(
            id = 1L,
            name = "document"
        )
        
        val originalFile = File(
            id = fileId,
            name = "Original File",
            type = fileType,
            authorId = mockUser.id,
            uploadDate = LocalDateTime.now()
        )
        
        val updatedFile = File(
            id = fileId,
            name = newName,
            type = fileType,
            authorId = mockUser.id,
            uploadDate = originalFile.uploadDate
        )
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description"
        )
        
        val projectFile = ProjectFile(
            id = 1L,
            project = project,
            file = originalFile
        )
        
        // Подготавливаем ожидаемый DTO ответа
        val expectedResponse = ProjectFileResponseDto(
            id = updatedFile.id,
            name = updatedFile.name,
            type = FileTypeResponseDto(id = fileType.id, name = fileType.name),
            authorId = updatedFile.authorId,
            date = updatedFile.uploadDate.toString(),
            superObjectId = null
        )
        
        // Настраиваем моки
        doNothing().`when`(projectService).checkAccessToProject(eq(projectId), eq(mockUser.id), anyList())
        `when`(projectFileRepository.findByProject_IdAndFile_Id(projectId, fileId)).thenReturn(projectFile)
        `when`(fileRepository.save(any(File::class.java))).thenReturn(updatedFile)
        
        // Act
        val result = projectFileService.updateFileNameInProject(projectId, fileId, newName)
        
        // Assert
        assertNotNull(result)
        assertEquals(expectedResponse.id, result.id)
        assertEquals(expectedResponse.name, result.name)
        
        verify(projectService).checkAccessToProject(eq(projectId), eq(mockUser.id), anyList())
        verify(projectFileRepository).findByProject_IdAndFile_Id(projectId, fileId)
        verify(fileRepository).save(any(File::class.java))
    }

    @Test
    fun `unlinkFile - should unlink file from project`() {
        // Arrange
        val projectId = 1L
        val fileId = 2L
        
        val fileType = FileType(
            id = 1L,
            name = "document"
        )
        
        val file = File(
            id = fileId,
            name = "File to Unlink",
            type = fileType,
            authorId = mockUser.id,
            uploadDate = LocalDateTime.now()
        )
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description"
        )
        
        val projectFile = ProjectFile(
            id = 1L,
            project = project,
            file = file
        )
        
        // Настраиваем моки
        doNothing().`when`(projectService).checkAccessToProject(eq(projectId), eq(mockUser.id), anyList())
        `when`(projectFileRepository.findByProject_IdAndFile_Id(projectId, fileId)).thenReturn(projectFile)
        
        // Act
        projectFileService.unlinkFile(projectId, fileId)
        
        // Assert
        verify(projectService).checkAccessToProject(eq(projectId), eq(mockUser.id), anyList())
        verify(projectFileRepository).findByProject_IdAndFile_Id(projectId, fileId)
        verify(projectFileRepository).delete(projectFile)
    }

    @Test
    fun `unlinkFile - when file not linked - should throw not found exception`() {
        // Arrange
        val projectId = 1L
        val fileId = 2L
        
        // Настраиваем моки
        doNothing().`when`(projectService).checkAccessToProject(eq(projectId), eq(mockUser.id), anyList())
        `when`(projectFileRepository.findByProject_IdAndFile_Id(projectId, fileId)).thenReturn(null)
        
        // Act & Assert
        val exception = assertFailsWith<ResponseStatusException> {
            projectFileService.unlinkFile(projectId, fileId)
        }
        
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertEquals("File is not linked to this project.", exception.reason)
        
        verify(projectService).checkAccessToProject(eq(projectId), eq(mockUser.id), anyList())
        verify(projectFileRepository).findByProject_IdAndFile_Id(projectId, fileId)
        verify(projectFileRepository, never()).delete(any(ProjectFile::class.java))
    }
}
