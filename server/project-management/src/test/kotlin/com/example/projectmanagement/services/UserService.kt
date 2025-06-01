package com.example.projectmanagement.services

import com.example.projectmanagement.controllers.dto.UserResponseDto
import com.example.projectmanagement.models.User
import com.example.projectmanagement.repositories.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserServiceTest {

    private lateinit var userService: UserService
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        userRepository = mock(UserRepository::class.java)
        userService = UserService(userRepository)
    }

    @Test
    fun `getUserById - when user exists - should return user`() {
        val userId = 1L
        val user = User(
            id = userId,
            name = "Test",
            surname = "User",
            login = "testuser",
            passwordInternal = "password"
        )
        
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))
        
        val result = userService.getUserById(userId)
        
        assertEquals(user, result)
        verify(userRepository).findById(userId)
    }

    @Test
    fun `getUserById - when user doesn't exist - should throw ResponseStatusException`() {
        val userId = 999L
        
        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())
        
        val exception = assertFailsWith<ResponseStatusException> {
            userService.getUserById(userId)
        }
        
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertEquals("User not found", exception.reason)
        
        verify(userRepository).findById(userId)
    }

    @Test
    fun `updateUser - when user exists - should update and return user`() {
        val userId = 1L
        val existingUser = User(
            id = userId,
            name = "Original",
            surname = "User",
            login = "testuser",
            passwordInternal = "password",
            photo = "old_photo.jpg"
        )
        
        val updateUser = User(
            id = userId,
            name = "Updated",
            surname = "Person",
            login = "testuser",
            passwordInternal = "password",
            photo = "new_photo.jpg"
        )
        
        val updatedUser = User(
            id = userId,
            name = "Updated",
            surname = "Person",
            login = "testuser",
            passwordInternal = "password",
            photo = "new_photo.jpg"
        )
        
        `when`(userRepository.existsById(userId)).thenReturn(true)
        `when`(userRepository.findById(userId)).thenReturn(Optional.of(existingUser))
        `when`(userRepository.save(any())).thenReturn(updatedUser)
        
        val result = userService.updateUser(userId, updateUser)
        
        assertEquals(updatedUser, result)
        assertEquals("Updated", result.name)
        assertEquals("Person", result.surname)
        assertEquals("new_photo.jpg", result.photo)
        
        verify(userRepository).existsById(userId)
        verify(userRepository).findById(userId)
        verify(userRepository).save(any())
    }

    @Test
    fun `updateUser - when user doesn't exist - should throw ResponseStatusException`() {
        val userId = 999L
        val updateUser = User(
            id = userId,
            name = "Updated",
            surname = "Person",
            login = "testuser",
            passwordInternal = "password"
        )
        
        `when`(userRepository.existsById(userId)).thenReturn(false)
        
        val exception = assertFailsWith<ResponseStatusException> {
            userService.updateUser(userId, updateUser)
        }
        
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertEquals("User not found", exception.reason)
        
        verify(userRepository).existsById(userId)
        verify(userRepository, never()).findById(any())
        verify(userRepository, never()).save(any())
    }

    @Test
    fun `deleteUser - when user exists - should delete user`() {
        val userId = 1L
        
        `when`(userRepository.existsById(userId)).thenReturn(true)
        
        userService.deleteUser(userId)
        
        verify(userRepository).existsById(userId)
        verify(userRepository).deleteById(userId)
    }

    @Test
    fun `deleteUser - when user doesn't exist - should throw ResponseStatusException`() {
        val userId = 999L
        
        `when`(userRepository.existsById(userId)).thenReturn(false)
        
        val exception = assertFailsWith<ResponseStatusException> {
            userService.deleteUser(userId)
        }
        
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertEquals("User not found", exception.reason)
        
        verify(userRepository).existsById(userId)
        verify(userRepository, never()).deleteById(any())
    }

    @Test
    fun `searchUsersByLogin - should return users matching login query`() {
        val loginQuery = "test"
        val users = listOf(
            User(
                id = 1L,
                name = "Test1",
                surname = "User1",
                login = "testuser1",
                passwordInternal = "password"
            ),
            User(
                id = 2L,
                name = "Test2",
                surname = "User2",
                login = "testuser2",
                passwordInternal = "password"
            )
        )
        
        val expectedResponses = listOf(
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
        
        `when`(userRepository.findByLoginContainingIgnoreCase(loginQuery)).thenReturn(users)
        
        val result = userService.searchUsersByLogin(loginQuery)
        
        assertEquals(expectedResponses.size, result.size)
        assertEquals(expectedResponses[0].id, result[0].id)
        assertEquals(expectedResponses[0].login, result[0].login)
        assertEquals(expectedResponses[1].id, result[1].id)
        assertEquals(expectedResponses[1].login, result[1].login)
        
        verify(userRepository).findByLoginContainingIgnoreCase(loginQuery)
    }
}
