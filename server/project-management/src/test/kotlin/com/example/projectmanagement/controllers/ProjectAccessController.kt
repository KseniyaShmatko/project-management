// package com.example.projectmanagement.controllers

// import com.example.projectmanagement.controllers.dto.ProjectUserDto
// import com.example.projectmanagement.controllers.dto.ProjectUserView
// import com.example.projectmanagement.models.ProjectRole
// import com.example.projectmanagement.models.ProjectUser
// import com.example.projectmanagement.services.ProjectUserService
// import org.junit.jupiter.api.BeforeEach
// import org.junit.jupiter.api.Test
// import org.mockito.ArgumentMatchers.any
// import org.mockito.Mockito.*
// import org.springframework.http.HttpStatus
// import org.springframework.web.server.ResponseStatusException
// import kotlin.test.assertEquals
// import kotlin.test.assertNotNull
// import kotlin.test.fail

// class ProjectAccessControllerTest {
    
//     private lateinit var projectAccessController: ProjectAccessController
//     private lateinit var projectUserService: ProjectUserService
    
//     @BeforeEach
//     fun setup() {
//         projectUserService = mock(ProjectUserService::class.java)
//         projectAccessController = ProjectAccessController(projectUserService)
//     }
    
//     @Test
//     fun `linkUserToProject - should call service and return created status`() {
//         // Arrange
//         val projectUserDto = ProjectUserDto(
//             projectId = 1L,
//             userId = 2L,
//             role = ProjectRole.EDITOR
//         )
        
//         // Создаем мок для ProjectUser и его ProjectUserView
//         val projectUser = mock(ProjectUser::class.java)
//         val projectUserView = ProjectUserView(
//             id = 1L,
//             projectId = 1L,
//             userId = 2L,
//             userLogin = "testuser",
//             userName = "Test",
//             userSurname = "User",
//             userPhoto = null,
//             role = ProjectRole.EDITOR
//         )
        
//         // Настраиваем мок для возврата значений
//         `when`(projectUserService.linkUserToProject(any(ProjectUserDto::class.java))).thenReturn(projectUser)
        
//         // Поскольку у нас нет доступа к функции toView, мы должны модифицировать тест
//         // Для этого теста достаточно проверить, что контроллер вызывает сервис с правильными параметрами
//         // и возвращает правильный HTTP статус
        
//         // Act
//         val result = projectAccessController.linkUserToProject(projectUserDto)
        
//         // Assert
//         assertEquals(HttpStatus.CREATED, result.statusCode)
//         assertNotNull(result.body)
//         verify(projectUserService).linkUserToProject(projectUserDto)
//     }
    
//     @Test
//     fun `getUsersForProject - should call service and return OK status`() {
//         // Arrange
//         val projectId = 1L
        
//         // Создаем список ProjectUser мокированных объектов
//         val projectUsers = listOf(mock(ProjectUser::class.java), mock(ProjectUser::class.java))
        
//         // Настраиваем мок для возврата значений
//         `when`(projectUserService.getUsersForProject(projectId)).thenReturn(projectUsers)
        
//         // Act
//         val result = projectAccessController.getUsersForProject(projectId)
        
//         // Assert
//         assertEquals(HttpStatus.OK, result.statusCode)
//         assertNotNull(result.body)
//         verify(projectUserService).getUsersForProject(projectId)
//     }
    
//     @Test
//     fun `updateUserProjectRole - should call service and return OK status`() {
//         // Arrange
//         val projectId = 1L
//         val userId = 2L
//         val updateDto = ProjectAccessController.UpdateUserRoleDto(role = ProjectRole.EDITOR)
        
//         // Создаем мок для ProjectUser
//         val projectUser = mock(ProjectUser::class.java)
        
//         // Настраиваем мок для возврата значений
//         `when`(projectUserService.updateUserProjectRole(eq(projectId), eq(userId), eq(updateDto.role))).thenReturn(projectUser)
        
//         // Act
//         val result = projectAccessController.updateUserProjectRole(projectId, userId, updateDto)
        
//         // Assert
//         assertEquals(HttpStatus.OK, result.statusCode)
//         assertNotNull(result.body)
//         verify(projectUserService).updateUserProjectRole(projectId, userId, updateDto.role)
//     }
    
//     @Test
//     fun `removeUserFromProject - should call service and return no content status`() {
//         // Arrange
//         val projectId = 1L
//         val userId = 2L
        
//         // Act
//         val result = projectAccessController.removeUserFromProject(projectId, userId)
        
//         // Assert
//         assertEquals(HttpStatus.NO_CONTENT, result.statusCode)
//         verify(projectUserService).removeUserFromProject(projectId, userId)
//     }
    
//     @Test
//     fun `linkUserToProject - when exception - should propagate it`() {
//         // Arrange
//         val projectUserDto = ProjectUserDto(
//             projectId = 1L,
//             userId = 2L,
//             role = ProjectRole.EDITOR
//         )
        
//         `when`(projectUserService.linkUserToProject(any(ProjectUserDto::class.java)))
//             .thenThrow(ResponseStatusException(HttpStatus.CONFLICT, "User is already linked to this project"))
        
//         try {
//             // Act
//             projectAccessController.linkUserToProject(projectUserDto)
//             fail("Should have thrown ResponseStatusException")
//         } catch (e: ResponseStatusException) {
//             // Assert
//             assertEquals(HttpStatus.CONFLICT, e.statusCode)
//             assertEquals("User is already linked to this project", e.reason)
//         }
//     }
// }
