package com.example.projectmanagement.security

import com.example.projectmanagement.models.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
import java.lang.reflect.Field
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var userDetailsService: UserDetailsService
    private val secretKeyString = "testSecretKeyWithAtLeast32Characters123456789012345678901234567890"

    @BeforeEach
    fun setup() {
        userDetailsService = mock(UserDetailsService::class.java)
        jwtTokenProvider = JwtTokenProvider(userDetailsService)
        
        val secretKeyStringField = JwtTokenProvider::class.java.getDeclaredField("secretKeyString")
        secretKeyStringField.isAccessible = true
        secretKeyStringField.set(jwtTokenProvider, secretKeyString)
        
        jwtTokenProvider.javaClass.getDeclaredMethod("init").apply {
            isAccessible = true
            invoke(jwtTokenProvider)
        }
    }

    @Test
    fun `createToken - should create token with correct format`() {
        val login = "testuser"
        val userId = 1L
        
        val token = jwtTokenProvider.createToken(login, userId)
        
        assertNotNull(token)
        assertTrue(token.isNotEmpty())
    }

    @Test
    fun `resolveToken - when valid bearer token - should return token`() {
        val token = "validToken123"
        val bearerToken = "Bearer $token"
        
        val request = mock(jakarta.servlet.http.HttpServletRequest::class.java)
        `when`(request.getHeader("Authorization")).thenReturn(bearerToken)
        
        val result = jwtTokenProvider.resolveToken(request)
        
        assertEquals(token, result)
    }

    @Test
    fun `resolveToken - when no bearer token - should return null`() {
        val request = mock(jakarta.servlet.http.HttpServletRequest::class.java)
        `when`(request.getHeader("Authorization")).thenReturn(null)
        
        val result = jwtTokenProvider.resolveToken(request)
        
        assertEquals(null, result)
    }

    @Test
    fun `resolveToken - when invalid bearer token format - should return null`() {
        val token = "validToken123"
        val invalidBearerToken = "Invalid $token"
        
        val request = mock(jakarta.servlet.http.HttpServletRequest::class.java)
        `when`(request.getHeader("Authorization")).thenReturn(invalidBearerToken)
        
        val result = jwtTokenProvider.resolveToken(request)
        
        assertEquals(null, result)
    }
}
