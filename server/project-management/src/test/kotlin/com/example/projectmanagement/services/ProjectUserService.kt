package com.example.projectmanagement.services

import com.example.projectmanagement.controllers.dto.ProjectUserDto
import com.example.projectmanagement.models.Project
import com.example.projectmanagement.models.ProjectRole
import com.example.projectmanagement.models.ProjectUser
import com.example.projectmanagement.models.User
import com.example.projectmanagement.repositories.ProjectRepository
import com.example.projectmanagement.repositories.ProjectUserRepository
import com.example.projectmanagement.repositories.UserRepository
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
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProjectUserServiceTest {

    private lateinit var projectUserService: ProjectUserService
    private lateinit var projectUserRepository: ProjectUserRepository
    private lateinit var projectRepository: ProjectRepository
    private lateinit var userRepository: UserRepository
    private lateinit var mockUser: User

    @BeforeEach
    fun setup() {
        SecurityContextHolder.clearContext()
        
        projectUserRepository = mock(ProjectUserRepository::class.java)
        projectRepository = mock(ProjectRepository::class.java)
        userRepository = mock(UserRepository::class.java)
        
        mockUser = User(
            id = 1L,
            name = "Test",
            surname = "User",
            login = "testuser",
            passwordInternal = "password"
        )
        
        val authentication = mock(Authentication::class.java)
        `when`(authentication.principal).thenReturn(mockUser)
        `when`(authentication.isAuthenticated).thenReturn(true)
        
        val securityContext = mock(SecurityContext::class.java)
        `when`(securityContext.authentication).thenReturn(authentication)
        
        SecurityContextHolder.setContext(securityContext)

        projectUserService = ProjectUserService(
            projectUserRepository,
            projectRepository,
            userRepository
        )
    }

    @Test
    fun `linkUserToProject - when current user is owner - should link user to project`() {
        val projectId = 1L
        val userIdToLink = 2L
        val projectUserDto = ProjectUserDto(
            projectId = projectId,
            userId = userIdToLink,
            role = ProjectRole.EDITOR
        )
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = mockUser
        )
        
        val userToLink = User(
            id = userIdToLink,
            name = "User",
            surname = "ToLink",
            login = "usertolink",
            passwordInternal = "password"
        )
        
        val projectUser = ProjectUser(
            id = 1L,
            project = project,
            user = userToLink,
            role = ProjectRole.EDITOR
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(userRepository.findById(userIdToLink)).thenReturn(Optional.of(userToLink))
        `when`(projectUserRepository.findByProject_IdAndUser_Id(projectId, userIdToLink)).thenReturn(null)
        `when`(projectUserRepository.save(any(ProjectUser::class.java))).thenReturn(projectUser)
        
        val result = projectUserService.linkUserToProject(projectUserDto)
        
        assertEquals(projectUser.id, result.id)
        assertEquals(projectUser.role, result.role)
        
        verify(userRepository).findById(userIdToLink)
        verify(projectUserRepository).findByProject_IdAndUser_Id(projectId, userIdToLink)
        verify(projectUserRepository).save(any(ProjectUser::class.java))
    }

    @Test
    fun `linkUserToProject - when current user is not owner - should throw AccessDeniedException`() {
        val projectId = 1L
        val userIdToLink = 2L
        val projectUserDto = ProjectUserDto(
            projectId = projectId,
            userId = userIdToLink,
            role = ProjectRole.EDITOR
        )
        
        val anotherUser = User(
            id = 3L,
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
        
        assertFailsWith<AccessDeniedException> {
            projectUserService.linkUserToProject(projectUserDto)
        }
        
        verify(projectRepository).findById(projectId)
        verify(projectUserRepository).findByProject_IdAndUser_Id(projectId, mockUser.id)
        verify(userRepository, never()).findById(anyLong())
        verify(projectUserRepository, never()).save(any(ProjectUser::class.java))
    }

    @Test
    fun `linkUserToProject - when try to assign OWNER role - should throw ResponseStatusException`() {
        val projectId = 1L
        val userIdToLink = 2L
        val projectUserDto = ProjectUserDto(
            projectId = projectId,
            userId = userIdToLink,
            role = ProjectRole.OWNER
        )
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = mockUser
        )
        
        val userToLink = User(
            id = userIdToLink,
            name = "User",
            surname = "ToLink",
            login = "usertolink",
            passwordInternal = "password"
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(userRepository.findById(userIdToLink)).thenReturn(Optional.of(userToLink))
        
        val exception = assertFailsWith<ResponseStatusException> {
            projectUserService.linkUserToProject(projectUserDto)
        }
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        assertEquals("Cannot assign OWNER role. This role is reserved for the project creator.", exception.reason)
        
        verify(userRepository).findById(userIdToLink)
        verify(projectUserRepository, never()).save(any(ProjectUser::class.java))
    }

    @Test
    fun `addOwnerAsProjectUser - should add owner as project user with OWNER role`() {
        val projectId = 1L
        val ownerId = 1L
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = mockUser
        )
        
        val projectUser = ProjectUser(
            id = 1L,
            project = project,
            user = mockUser,
            role = ProjectRole.OWNER
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(userRepository.findById(ownerId)).thenReturn(Optional.of(mockUser))
        `when`(projectUserRepository.save(any(ProjectUser::class.java))).thenReturn(projectUser)
        
        val result = projectUserService.addOwnerAsProjectUser(projectId, ownerId)
        
        assertEquals(projectUser.id, result.id)
        assertEquals(ProjectRole.OWNER, result.role)
        
        verify(projectRepository).findById(projectId)
        verify(userRepository).findById(ownerId)
        verify(projectUserRepository).save(any(ProjectUser::class.java))
    }

    @Test
    fun `getUsersForProject - when user is participant - should return project users`() {
        val projectId = 1L
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = User(id = 2L, name = "Owner", surname = "User", login = "owner", passwordInternal = "password")
        )
        
        val projectUser = ProjectUser(
            id = 1L,
            project = project,
            user = mockUser,
            role = ProjectRole.EDITOR
        )
        
        val projectUsers = listOf(
            projectUser,
            ProjectUser(
                id = 2L,
                project = project,
                user = User(id = 3L, name = "Another", surname = "User", login = "another", passwordInternal = "password"),
                role = ProjectRole.VIEWER
            )
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(projectRepository.existsByIdAndOwner_Id(projectId, mockUser.id)).thenReturn(false)
        `when`(projectUserRepository.existsByProject_IdAndUser_Id(projectId, mockUser.id)).thenReturn(true)
        `when`(projectUserRepository.findAllByProject_Id(projectId)).thenReturn(projectUsers)
        
        val result = projectUserService.getUsersForProject(projectId)
        
        assertEquals(projectUsers.size, result.size)
        assertEquals(projectUsers[0].id, result[0].id)
        assertEquals(projectUsers[1].id, result[1].id)
        
        verify(projectRepository).findById(projectId)
        verify(projectRepository).existsByIdAndOwner_Id(projectId, mockUser.id)
        verify(projectUserRepository).existsByProject_IdAndUser_Id(projectId, mockUser.id)
        verify(projectUserRepository).findAllByProject_Id(projectId)
    }

    @Test
    fun `getUsersForProject - when user is not participant - should throw AccessDeniedException`() {
        val projectId = 1L
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = User(id = 2L, name = "Owner", surname = "User", login = "owner", passwordInternal = "password")
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(projectRepository.existsByIdAndOwner_Id(projectId, mockUser.id)).thenReturn(false)
        `when`(projectUserRepository.existsByProject_IdAndUser_Id(projectId, mockUser.id)).thenReturn(false)
        
        assertFailsWith<AccessDeniedException> {
            projectUserService.getUsersForProject(projectId)
        }
        
        verify(projectRepository).findById(projectId)
        verify(projectRepository).existsByIdAndOwner_Id(projectId, mockUser.id)
        verify(projectUserRepository).existsByProject_IdAndUser_Id(projectId, mockUser.id)
        verify(projectUserRepository, never()).findAllByProject_Id(anyLong())
    }

    @Test
    fun `updateUserProjectRole - when current user is owner - should update role`() {
        val projectId = 1L
        val userIdToUpdate = 2L
        val newRole = ProjectRole.EDITOR
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = mockUser
        )
        
        val userToUpdate = User(
            id = userIdToUpdate,
            name = "User",
            surname = "ToUpdate",
            login = "usertoupdate",
            passwordInternal = "password"
        )
        
        val projectUser = ProjectUser(
            id = 1L,
            project = project,
            user = userToUpdate,
            role = ProjectRole.VIEWER
        )
        
        val updatedProjectUser = ProjectUser(
            id = 1L,
            project = project,
            user = userToUpdate,
            role = ProjectRole.EDITOR
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(projectUserRepository.findByProject_IdAndUser_Id(projectId, userIdToUpdate)).thenReturn(projectUser)
        `when`(projectUserRepository.save(any(ProjectUser::class.java))).thenReturn(updatedProjectUser)
        
        val result = projectUserService.updateUserProjectRole(projectId, userIdToUpdate, newRole)
        
        assertEquals(updatedProjectUser.id, result.id)
        assertEquals(newRole, result.role)
        
        verify(projectRepository).findById(projectId)
        verify(projectUserRepository).findByProject_IdAndUser_Id(projectId, userIdToUpdate)
        verify(projectUserRepository).save(any(ProjectUser::class.java))
    }

    @Test
    fun `removeUserFromProject - when current user is owner - should remove user`() {
        val projectId = 1L
        val userIdToRemove = 2L
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = mockUser
        )
        
        val userToRemove = User(
            id = userIdToRemove,
            name = "User",
            surname = "ToRemove",
            login = "usertoremove",
            passwordInternal = "password"
        )
        
        val projectUser = ProjectUser(
            id = 1L,
            project = project,
            user = userToRemove,
            role = ProjectRole.EDITOR
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(projectUserRepository.findByProject_IdAndUser_Id(projectId, userIdToRemove)).thenReturn(projectUser)
        
        projectUserService.removeUserFromProject(projectId, userIdToRemove)
        
        verify(projectRepository).findById(projectId)
        verify(projectUserRepository).findByProject_IdAndUser_Id(projectId, userIdToRemove)
        verify(projectUserRepository).delete(projectUser)
    }

    @Test
    fun `removeUserFromProject - when try to remove owner - should throw ResponseStatusException`() {
        val projectId = 1L
        val ownerIdToRemove = 1L
        
        val project = Project(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = mockUser
        )
        
        val projectUser = ProjectUser(
            id = 1L,
            project = project,
            user = mockUser,
            role = ProjectRole.OWNER
        )
        
        `when`(projectRepository.findById(projectId)).thenReturn(Optional.of(project))
        `when`(projectUserRepository.findByProject_IdAndUser_Id(projectId, ownerIdToRemove)).thenReturn(projectUser)
        
        val exception = assertFailsWith<ResponseStatusException> {
            projectUserService.removeUserFromProject(projectId, ownerIdToRemove)
        }
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        assertEquals("Cannot remove the project owner. To remove the owner, delete the project or transfer ownership (if implemented).", exception.reason)
        
        verify(projectRepository).findById(projectId)
        verify(projectUserRepository).findByProject_IdAndUser_Id(projectId, ownerIdToRemove)
        verify(projectUserRepository, never()).delete(any(ProjectUser::class.java))
    }
}
