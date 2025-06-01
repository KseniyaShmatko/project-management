package com.example.projectmanagement.controllers

import com.example.projectmanagement.controllers.dto.UserResponseDto
import com.example.projectmanagement.models.User
import com.example.projectmanagement.services.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.fail

class UserControllerTest {
    
    private lateinit var userController: UserController
    private lateinit var userService: UserService
    
    @BeforeEach
    fun setup() {
        userService = mock(UserService::class.java)
        userController = UserController(userService)
    }
    
    @Test
    fun `searchUsersByLogin - should return users by login query`() {
        val loginQuery = "test"
        val users = listOf(
            UserResponseDto(
                id = 1L,
                name = "Test1",
                surname = "User1",
                login = "testuser1",
                photo = null
            ),
            UserResponseDto(
                id = 2L,
                name = "Test2",
                surname = "User2",
                login = "testuser2",
                photo = null
            )
        )
        
        `when`(userService.searchUsersByLogin(loginQuery)).thenReturn(users)
        
        val result = userController.searchUsersByLogin(loginQuery)
        
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(users, result.body)
    }
    
    @Test
    fun `getUser - should return user by id`() {
        val userId = 1L
        val user = User(
            id = userId,
            name = "Test",
            surname = "User",
            login = "testuser",
            passwordInternal = "password"
        )
        
        `when`(userService.getUserById(userId)).thenReturn(user)
        
        val result = userController.getUser(userId)
        
        assertEquals(user, result)
    }
    
    @Test
    fun `updateUser - should update and return user`() {
        val userId = 1L
        val updateUser = User(
            id = userId,
            name = "Updated",
            surname = "User",
            login = "testuser",
            passwordInternal = "password",
            photo = "new_photo.jpg"
        )
        
        `when`(userService.updateUser(userId, updateUser)).thenReturn(updateUser)
        
        val result = userController.updateUser(userId, updateUser)
        
        assertEquals(updateUser, result)
    }
    
    @Test
    fun `deleteUser - should delete user`() {
        val userId = 1L
        
        userController.deleteUser(userId)
        
        verify(userService).deleteUser(userId)
    }
    
    @Test
    fun `getUser - when user doesn't exist - should propagate exception`() {
        val userId = 999L
        
        `when`(userService.getUserById(userId))
            .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"))
        
        try {

            userController.getUser(userId)
            fail("Should have thrown ResponseStatusException")
        } catch (e: ResponseStatusException) {

            assertEquals(HttpStatus.NOT_FOUND, e.statusCode)
            assertEquals("User not found", e.reason)
        }
    }
}
