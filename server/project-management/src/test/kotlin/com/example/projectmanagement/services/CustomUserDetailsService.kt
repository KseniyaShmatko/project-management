package com.example.projectmanagement.services

import com.example.projectmanagement.models.User
import com.example.projectmanagement.repositories.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CustomUserDetailsServiceTest {

    private lateinit var customUserDetailsService: CustomUserDetailsService
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        userRepository = mock(UserRepository::class.java)
        customUserDetailsService = CustomUserDetailsService(userRepository)
    }

    @Test
    fun `loadUserByUsername - when user exists - should return user`() {
        val username = "testuser"
        val user = User(
            id = 1L,
            name = "Test",
            surname = "User",
            login = username,
            passwordInternal = "password"
        )
        
        `when`(userRepository.findByLogin(username)).thenReturn(Optional.of(user))
        
        val result = customUserDetailsService.loadUserByUsername(username)
        
        assertEquals(user, result)
        verify(userRepository).findByLogin(username)
    }

    @Test
    fun `loadUserByUsername - when user doesn't exist - should throw UsernameNotFoundException`() {
        val username = "nonexistentuser"
        
        `when`(userRepository.findByLogin(username)).thenReturn(Optional.empty())
        
        val exception = assertFailsWith<UsernameNotFoundException> {
            customUserDetailsService.loadUserByUsername(username)
        }
        
        assertEquals("User not found with login: $username", exception.message)
        verify(userRepository).findByLogin(username)
    }
}
