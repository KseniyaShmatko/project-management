package com.example.projectmanagement.services

import com.example.projectmanagement.controllers.dto.*
import com.example.projectmanagement.models.*
import com.example.projectmanagement.repositories.ProjectRepository
import com.example.projectmanagement.repositories.ProjectUserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import java.lang.reflect.InvocationTargetException
import org.junit.jupiter.api.Assertions.assertThrows

class ProjectServiceTest {
    
    private lateinit var projectService: ProjectService
    private lateinit var projectRepository: ProjectRepository
    private lateinit var projectUserRepository: ProjectUserRepository
    private lateinit var projectUserService: ProjectUserService
    private lateinit var mockUser: User

    @BeforeEach
    fun setup() {
        SecurityContextHolder.clearContext()
        
        projectRepository = mock(ProjectRepository::class.java)
        projectUserRepository = mock(ProjectUserRepository::class.java)
        projectUserService = mock(ProjectUserService::class.java)
        
        mockUser = User(
            id = 1L,
            name = "Test",
            surname = "User",
            login = "testuser",
            passwordInternal = "password"
        )
        
        val authentication = mock(Authentication::class.java)
        val securityContext = mock(SecurityContext::class.java)
        
        `when`(authentication.principal).thenReturn(mockUser)
        `when`(authentication.isAuthenticated).thenReturn(true)
        `when`(securityContext.authentication).thenReturn(authentication)
        
        SecurityContextHolder.setContext(securityContext)
        
        projectService = ProjectService(
            projectRepository,
            projectUserRepository,
            projectUserService
        )
    }

    @Test
    fun `createProject - should create and return project`() {
        val projectRequest = Project(
            name = "Test Project",
            description = "Test Description"
        )
        
        val savedProject = Project(
            id = 1L,
            name = "Test Project",
            description = "Test Description",
            owner = mockUser
        )
        
        val detailedProject = Project(
            id = 1L,
            name = "Test Project",
            description = "Test Description",
            owner = mockUser,
            projectUsers = mutableSetOf(),
            projectFiles = mutableSetOf()
        )
        
        `when`(projectRepository.save(any(Project::class.java))).thenReturn(savedProject)
        `when`(projectRepository.findProjectByIdWithDetails(savedProject.id)).thenReturn(Optional.of(detailedProject))
        
        val result = projectService.createProject(projectRequest)
        
        assertEquals(savedProject.id, result.id)
        assertEquals(savedProject.name, result.name)
        assertEquals(savedProject.description, result.description)
        
        verify(projectRepository).save(any(Project::class.java))
        verify(projectUserService).addOwnerAsProjectUser(savedProject.id, mockUser.id)
        verify(projectRepository).findProjectByIdWithDetails(savedProject.id)
    }

    @Test
    fun `getProjectById - when user has access - should return project`() {
        val projectId = 1L
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = mockUser,
            projectUsers = mutableSetOf(),
            projectFiles = mutableSetOf()
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(projectRepository.findProjectByIdWithDetails(projectId)).thenReturn(Optional.of(project))
        
        val result = projectService.getProjectById(projectId)
        
        assertEquals(projectId, result.id)
        assertEquals(project.name, result.name)
        assertEquals(project.description, result.description)
        
        verify(projectRepository).findProjectByIdWithDetails(projectId)
    }

    @Test
    fun `getProjectById - when user has no access - should throw AccessDeniedException`() {
        val projectId = 1L
        val anotherUser = User(
            id = 2L,
            name = "Another",
            surname = "User",
            login = "anotheruser",
            passwordInternal = "password"
        )
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = anotherUser
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(projectUserRepository.findByProject_IdAndUser_Id(projectId, mockUser.id)).thenReturn(null)
        
        assertFailsWith<AccessDeniedException> {
            projectService.getProjectById(projectId)
        }
        
        verify(projectRepository).findById(projectId)
        verify(projectUserRepository).findByProject_IdAndUser_Id(projectId, mockUser.id)
    }

    @Test
    fun `updateProject - when user has editor role - should update and return project`() {
        val projectId = 1L
        val updateData = Project(
            name = "Updated Project",
            description = "Updated Description"
        )
        
        val existingProject = Project(
            id = projectId,
            name = "Original Project",
            description = "Original Description",
            owner = User(id = 2L, name = "Owner", surname = "User", login = "owner", passwordInternal = "password")
        )
        
        val updatedProject = Project(
            id = projectId,
            name = "Updated Project",
            description = "Updated Description",
            owner = existingProject.owner
        )
        
        val projectUser = ProjectUser(
            id = 1L,
            project = existingProject,
            user = mockUser,
            role = ProjectRole.EDITOR
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(existingProject))
        `when`(projectUserRepository.findByProject_IdAndUser_Id(projectId, mockUser.id)).thenReturn(projectUser)
        `when`(projectRepository.save(any(Project::class.java))).thenReturn(updatedProject)
        `when`(projectRepository.findProjectByIdWithDetails(projectId)).thenReturn(Optional.of(updatedProject))
        
        val result = projectService.updateProject(projectId, updateData)
        
        assertEquals(projectId, result.id)
        assertEquals(updateData.name, result.name)
        assertEquals(updateData.description, result.description)
        

        verify(projectUserRepository).findByProject_IdAndUser_Id(projectId, mockUser.id)
        verify(projectRepository).save(any(Project::class.java))
        verify(projectRepository).findProjectByIdWithDetails(projectId)
    }

    @Test
    fun `deleteProject - when user is owner - should delete project`() {
        val projectId = 1L
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = mockUser
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        
        projectService.deleteProject(projectId)
        
        verify(projectRepository).deleteById(projectId)
    }

    @Test
    fun `deleteProject - when user is not owner - should throw AccessDeniedException`() {
        val projectId = 1L
        val anotherUser = User(
            id = 2L,
            name = "Another",
            surname = "User",
            login = "anotheruser",
            passwordInternal = "password"
        )
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = anotherUser
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(projectUserRepository.findByProject_IdAndUser_Id(projectId, mockUser.id)).thenReturn(
            ProjectUser(
                id = 1L,
                project = project,
                user = mockUser,
                role = ProjectRole.EDITOR
            )
        )
        
        assertFailsWith<AccessDeniedException> {
            projectService.deleteProject(projectId)
        }
        
        verify(projectUserRepository).findByProject_IdAndUser_Id(projectId, mockUser.id)
        verify(projectRepository, never()).deleteById(anyLong())
    }

    @Test
    fun `checkAccessToProject - when user is owner - should not throw exception`() {
        val projectId = 1L
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = mockUser
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        
        projectService.checkAccessToProject(projectId, mockUser.id, ProjectRole.OWNER)
        
        verify(projectRepository).findById(projectId)
        verify(projectUserRepository, never()).findByProject_IdAndUser_Id(anyLong(), anyLong())
    }

    @Test
    fun `checkAccessToProject - when user has required role - should not throw exception`() {
        val projectId = 1L
        val anotherUser = User(
            id = 2L,
            name = "Another",
            surname = "User",
            login = "anotheruser",
            passwordInternal = "password"
        )
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = anotherUser
        )
        
        val projectUser = ProjectUser(
            id = 1L,
            project = project,
            user = mockUser,
            role = ProjectRole.EDITOR
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(projectUserRepository.findByProject_IdAndUser_Id(projectId, mockUser.id)).thenReturn(projectUser)
        
        projectService.checkAccessToProject(projectId, mockUser.id, ProjectRole.EDITOR)
        
        verify(projectRepository).findById(projectId)
        verify(projectUserRepository).findByProject_IdAndUser_Id(projectId, mockUser.id)
    }

    @Test
    fun `checkAccessToProject - when user doesn't have required role - should throw AccessDeniedException`() {
        val projectId = 1L
        val anotherUser = User(
            id = 2L,
            name = "Another",
            surname = "User",
            login = "anotheruser",
            passwordInternal = "password"
        )
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = anotherUser
        )
        
        val projectUser = ProjectUser(
            id = 1L,
            project = project,
            user = mockUser,
            role = ProjectRole.VIEWER
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(projectUserRepository.findByProject_IdAndUser_Id(projectId, mockUser.id)).thenReturn(projectUser)
        
        assertFailsWith<AccessDeniedException> {
            projectService.checkAccessToProject(projectId, mockUser.id, ProjectRole.EDITOR)
        }
        
        verify(projectRepository).findById(projectId)
        verify(projectUserRepository).findByProject_IdAndUser_Id(projectId, mockUser.id)
    }

    @Test
    fun `checkAccessToProject - when project doesn't exist - should throw ResponseStatusException`() {
        val projectId = 999L
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.empty())
        
        val exception = assertFailsWith<ResponseStatusException> {
            projectService.checkAccessToProject(projectId, mockUser.id, ProjectRole.VIEWER)
        }
        
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertEquals("Project not found", exception.reason)
        
        verify(projectRepository).findById(projectId)
        verify(projectUserRepository, never()).findByProject_IdAndUser_Id(anyLong(), anyLong())
    }
    
    @Test
    fun `getCurrentUser - when authenticated - should return user`() {
        // Act
        val result = projectService.javaClass.getDeclaredMethod("getCurrentUser").apply {
            isAccessible = true
        }.invoke(projectService) as User
        
        // Assert
        assertEquals(mockUser.id, result.id)
        assertEquals(mockUser.name, result.name)
        assertEquals(mockUser.login, result.login)
    }

    @Test
    fun `getCurrentUser - when not authenticated - should throw IllegalStateException`() {
        // Arrange
        val authentication = mock(Authentication::class.java)
        val securityContext = mock(SecurityContext::class.java)
        
        `when`(authentication.isAuthenticated).thenReturn(false)
        `when`(securityContext.authentication).thenReturn(authentication)
        
        SecurityContextHolder.setContext(securityContext)
        
        // Act & Assert
        val method = projectService.javaClass.getDeclaredMethod("getCurrentUser").apply {
            isAccessible = true
        }
        
        val exception = assertThrows(InvocationTargetException::class.java) {
            method.invoke(projectService)
        }
        
        val cause = exception.targetException
        assertTrue(cause is IllegalStateException)
        assertTrue(cause.message?.contains("User not authenticated") == true)
    }

    @Test
    fun `getCurrentUser - when principal is not User - should throw IllegalStateException`() {
        // Arrange
        val authentication = mock(Authentication::class.java)
        val securityContext = mock(SecurityContext::class.java)
        
        `when`(authentication.isAuthenticated).thenReturn(true)
        `when`(authentication.principal).thenReturn("anonymousUser")
        `when`(securityContext.authentication).thenReturn(authentication)
        
        SecurityContextHolder.setContext(securityContext)
        
        // Act & Assert
        val method = projectService.javaClass.getDeclaredMethod("getCurrentUser").apply {
            isAccessible = true
        }
        
        val exception = assertThrows(InvocationTargetException::class.java) {
            method.invoke(projectService)
        }
        
        val cause = exception.targetException
        assertTrue(cause is IllegalStateException)
    }

    @Test
    fun `getProjectsForCurrentUser - should return projects for current user`() {
        // Arrange
        val project1 = Project(
            id = 1L,
            name = "Project 1",
            description = "Description 1",
            owner = mockUser,
            projectUsers = mutableSetOf(),
            projectFiles = mutableSetOf()
        )
        
        val project2 = Project(
            id = 2L,
            name = "Project 2",
            description = "Description 2",
            owner = User(id = 2L, name = "Another", surname = "User", login = "another", passwordInternal = "password"),
            projectUsers = mutableSetOf(
                ProjectUser(
                    id = 1L,
                    project = Project(id = 2L, name = "Project 2", description = "Description 2"),
                    user = mockUser,
                    role = ProjectRole.EDITOR
                )
            ),
            projectFiles = mutableSetOf()
        )
        
        val participatedProjects = listOf(project1, project2)
        
        `when`(projectRepository.findParticipatedProjectsByUserIdWithDetails(mockUser.id)).thenReturn(participatedProjects)
        
        // Act
        val result = projectService.getProjectsForCurrentUser()
        
        // Assert
        assertEquals(2, result.size)
        assertEquals(project1.id, result[0].id)
        assertEquals(project1.name, result[0].name)
        assertEquals(project2.id, result[1].id)
        assertEquals(project2.name, result[1].name)
        
        verify(projectRepository).findParticipatedProjectsByUserIdWithDetails(mockUser.id)
    }

    @Test
    fun `projectToProjectResponseDto - when user is owner - should set owner role`() {
        // Arrange
        val project = Project(
            id = 1L,
            name = "Test Project",
            description = "Test Description",
            owner = mockUser,
            projectUsers = mutableSetOf(),
            projectFiles = mutableSetOf()
        )
        
        // Act
        val result = projectService.projectToProjectResponseDto(project, mockUser)
        
        // Assert
        assertEquals(project.id, result.id)
        assertEquals(project.name, result.name)
        assertEquals(project.description, result.description)
        assertEquals(mockUser.id, result.owner?.id)
        assertEquals(ProjectRole.OWNER, result.currentUserRole)
        assertEquals(0, result.participants.size)
        assertEquals(0, result.projectFiles.size)
    }

    @Test
    fun `projectToProjectResponseDto - when user is participant - should set user role`() {
        // Arrange
        val anotherUser = User(
            id = 2L,
            name = "Another",
            surname = "User",
            login = "another",
            passwordInternal = "password"
        )
        
        val project = Project(
            id = 1L,
            name = "Test Project",
            description = "Test Description",
            owner = anotherUser
        )
        
        val projectUser = ProjectUser(
            id = 1L,
            project = project,
            user = mockUser,
            role = ProjectRole.EDITOR
        )
        
        project.projectUsers = mutableSetOf(projectUser)
        
        // Act
        val result = projectService.projectToProjectResponseDto(project, mockUser)
        
        // Assert
        assertEquals(project.id, result.id)
        assertEquals(project.name, result.name)
        assertEquals(project.description, result.description)
        assertEquals(anotherUser.id, result.owner?.id)
        assertEquals(ProjectRole.EDITOR, result.currentUserRole)
        assertEquals(1, result.participants.size)
        assertEquals(mockUser.id, result.participants[0].userId)
        assertEquals(mockUser.login, result.participants[0].login)
        assertEquals(ProjectRole.EDITOR, result.participants[0].role)
    }

    @Test
    fun `projectToProjectResponseDto - when user is not participant - should set null role`() {
        // Arrange
        val anotherUser = User(
            id = 2L,
            name = "Another",
            surname = "User",
            login = "another",
            passwordInternal = "password"
        )
        
        val thirdUser = User(
            id = 3L,
            name = "Third",
            surname = "User",
            login = "third",
            passwordInternal = "password"
        )
        
        val project = Project(
            id = 1L,
            name = "Test Project",
            description = "Test Description",
            owner = anotherUser
        )
        
        val projectUser = ProjectUser(
            id = 1L,
            project = project,
            user = thirdUser,
            role = ProjectRole.EDITOR
        )
        
        project.projectUsers = mutableSetOf(projectUser)
        
        // Act
        val result = projectService.projectToProjectResponseDto(project, mockUser)
        
        // Assert
        assertEquals(project.id, result.id)
        assertEquals(project.name, result.name)
        assertEquals(project.description, result.description)
        assertEquals(anotherUser.id, result.owner?.id)
        assertNull(result.currentUserRole)
        assertEquals(1, result.participants.size)
        assertEquals(thirdUser.id, result.participants[0].userId)
    }

    @Test
    fun `projectToProjectResponseDto - with project files - should map files correctly`() {
        // Arrange
        val fileType = FileType(id = 1L, name = "note")
        
        val file = File(
            id = 1L,
            name = "Test File",
            type = fileType,
            authorId = mockUser.id,
            uploadDate = LocalDateTime.now()
        )

        val projectFile = ProjectFile(
            id = 1L,
            project = Project(id = 1L, name = "Test Project", description = "Test Description"),
            file = file
        )
        val project = Project(
            id = 1L,
            name = "Test Project",
            description = "Test Description",
            owner = mockUser,
            projectUsers = mutableSetOf(),
            projectFiles = mutableSetOf(projectFile)
        )
        
        // Act
        val result = projectService.projectToProjectResponseDto(project, mockUser)
        
        // Assert
        assertEquals(1, result.projectFiles.size)
        assertEquals(file.id, result.projectFiles[0].id)
        assertEquals(file.name, result.projectFiles[0].name)
        assertEquals(fileType.id, result.projectFiles[0].type.id)
        assertEquals(fileType.name, result.projectFiles[0].type.name)
        assertEquals(file.authorId, result.projectFiles[0].authorId)
    }

    @Test
    fun `toProjectFileResponseDto - should convert ProjectFile to DTO`() {
        // Arrange
        val fileType = FileType(id = 1L, name = "note")
        
        val file = File(
            id = 1L,
            name = "Test File",
            type = fileType,
            authorId = mockUser.id,
            uploadDate = LocalDateTime.now(),
            superObjectId = "super123"
        )
        
        val projectFile = ProjectFile(
            id = 1L,
            project = Project(id = 1L, name = "Test Project", description = "Test Description"),
            file = file
        )
        
        // Act
        val result = projectService.javaClass.getDeclaredMethod("toProjectFileResponseDto", ProjectFile::class.java).apply {
            isAccessible = true
        }.invoke(projectService, projectFile) as ProjectFileResponseDto
        
        // Assert
        assertEquals(file.id, result.id)
        assertEquals(file.name, result.name)
        assertEquals(fileType.id, result.type.id)
        assertEquals(fileType.name, result.type.name)
        assertEquals(file.authorId, result.authorId)
        assertEquals(file.uploadDate.toString(), result.date)
        assertEquals(file.superObjectId, result.superObjectId)
    }

    @Test
    fun `toProjectFileResponseDto - when file type is null - should throw IllegalStateException`() {
        // Arrange
        val file = File(
            id = 1L,
            name = "Test File",
            type = null, // Null type
            authorId = mockUser.id,
            uploadDate = LocalDateTime.now()
        )
        
        val projectFile = ProjectFile(
            id = 1L,
            project = Project(id = 1L, name = "Test Project", description = "Test Description"),
            file = file
        )
        
        // Act & Assert
        val method = projectService.javaClass.getDeclaredMethod("toProjectFileResponseDto", ProjectFile::class.java).apply {
            isAccessible = true
        }
        
        val exception = assertThrows(InvocationTargetException::class.java) {
            method.invoke(projectService, projectFile)
        }
        
        val cause = exception.targetException
        assertTrue(cause is IllegalStateException)
        assertTrue(cause.message?.contains("FileType entity is null") == true)
    }

    @Test
    fun `toUserResponseDto - should convert User to DTO`() {
        // Arrange
        val user = User(
            id = 1L,
            name = "Test",
            surname = "User",
            login = "testuser",
            passwordInternal = "password",
            photo = "photo.jpg"
        )
        
        // Act
        val result = projectService.javaClass.getDeclaredMethod("toUserResponseDto", User::class.java).apply {
            isAccessible = true
        }.invoke(projectService, user) as UserResponseDto
        
        // Assert
        assertEquals(user.id, result.id)
        assertEquals(user.name, result.name)
        assertEquals(user.surname, result.surname)
        assertEquals(user.login, result.login)
        assertEquals(user.photo, result.photo)
    }


    @Test
    fun `toFileTypeResponseDto - should convert FileType to DTO`() {
        // Arrange
        val fileType = FileType(id = 1L, name = "note")
        
        // Act
        val result = projectService.javaClass.getDeclaredMethod("toFileTypeResponseDto", FileType::class.java).apply {
            isAccessible = true
        }.invoke(projectService, fileType) as FileTypeResponseDto
        
        // Assert
        assertEquals(fileType.id, result.id)
        assertEquals(fileType.name, result.name)
    }
}
