package com.example.projectmanagement.controllers

import com.example.projectmanagement.controllers.dto.ProjectResponseDto
import com.example.projectmanagement.controllers.dto.UserResponseDto
import com.example.projectmanagement.models.Project
import com.example.projectmanagement.models.User
import com.example.projectmanagement.services.ProjectService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.fail

class ProjectControllerTest {

    private lateinit var projectController: ProjectController
    private lateinit var projectService: ProjectService
    
    @BeforeEach
    fun setup() {
        projectService = mock(ProjectService::class.java)
        projectController = ProjectController(projectService)
    }
    
    @Test
    fun `createProject - should create and return project`() {
        val project = Project(
            name = "Test Project",
            description = "Test Description"
        )
        
        val ownerDto = UserResponseDto(
            id = 1L,
            name = "Test",
            surname = "User",
            login = "testuser",
            photo = null
        )
        
        val projectResponseDto = ProjectResponseDto(
            id = 1L,
            name = project.name,
            description = project.description,
            owner = ownerDto,
            projectFiles = emptyList(),
            participants = emptyList(),
            currentUserRole = null
        )
        
        `when`(projectService.createProject(project)).thenReturn(projectResponseDto)
        
        val result = projectController.createProject(project)
        
        assertEquals(HttpStatus.CREATED, result.statusCode)
        assertEquals(projectResponseDto, result.body)
    }
    
    @Test
    fun `getCurrentUserProjects - should return projects for current user`() {
        val projects = listOf(
            ProjectResponseDto(
                id = 1L,
                name = "Project 1",
                description = "Description 1",
                owner = null,
                projectFiles = emptyList(),
                participants = emptyList(),
                currentUserRole = null
            ),
            ProjectResponseDto(
                id = 2L,
                name = "Project 2",
                description = "Description 2",
                owner = null,
                projectFiles = emptyList(),
                participants = emptyList(),
                currentUserRole = null
            )
        )
        
        `when`(projectService.getProjectsForCurrentUser()).thenReturn(projects)
        
        val result = projectController.getCurrentUserProjects()
        
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(projects, result.body)
    }
    
    @Test
    fun `getProject - should return project by id`() {
        val projectId = 1L
        val projectResponseDto = ProjectResponseDto(
            id = projectId,
            name = "Test Project",
            description = "Test Description",
            owner = null,
            projectFiles = emptyList(),
            participants = emptyList(),
            currentUserRole = null
        )
        
        `when`(projectService.getProjectById(projectId)).thenReturn(projectResponseDto)
        
        val result = projectController.getProject(projectId)
        
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(projectResponseDto, result.body)
    }
    
    @Test
    fun `getProject - when project doesn't exist - should propagate exception`() {
        val projectId = 999L
        `when`(projectService.getProjectById(projectId)).thenThrow(
            ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found")
        )
        
        try {

            projectController.getProject(projectId)
            fail("Should have thrown ResponseStatusException")
        } catch (e: ResponseStatusException) {

            assertEquals(HttpStatus.NOT_FOUND, e.statusCode)
            assertEquals("Project not found", e.reason)
        }
    }
    
    @Test
    fun `updateProject - should update and return project`() {
        val projectId = 1L
        val updateProject = Project(
            name = "Updated Project",
            description = "Updated Description"
        )
        
        val updatedProjectResponseDto = ProjectResponseDto(
            id = projectId,
            name = updateProject.name,
            description = updateProject.description,
            owner = null,
            projectFiles = emptyList(),
            participants = emptyList(),
            currentUserRole = null
        )
        
        `when`(projectService.updateProject(projectId, updateProject)).thenReturn(updatedProjectResponseDto)
        
        val result = projectController.updateProject(projectId, updateProject)
        
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(updatedProjectResponseDto, result.body)
    }
    
    @Test
    fun `deleteProject - should delete project and return no content`() {
        val projectId = 1L
        
        val result = projectController.deleteProject(projectId)
        
        assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
        verify(projectService).deleteProject(projectId)
    }
}
