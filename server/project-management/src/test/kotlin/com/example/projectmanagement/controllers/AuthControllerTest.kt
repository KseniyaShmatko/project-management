package com.example.projectmanagement.controllers

import com.example.projectmanagement.controllers.dto.JwtResponse
import com.example.projectmanagement.controllers.dto.LoginRequest
import com.example.projectmanagement.controllers.dto.RegisterRequest
import com.example.projectmanagement.models.User
import com.example.projectmanagement.repositories.UserRepository
import com.example.projectmanagement.security.JwtTokenProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthControllerTest {

    private lateinit var authController: AuthController
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    
    @BeforeEach
    fun setup() {
        authenticationManager = mock(AuthenticationManager::class.java)
        jwtTokenProvider = mock(JwtTokenProvider::class.java)
        userRepository = mock(UserRepository::class.java)
        passwordEncoder = mock(PasswordEncoder::class.java)
        
        authController = AuthController(
            authenticationManager,
            jwtTokenProvider,
            userRepository,
            passwordEncoder
        )
    }
    
    @Test
    fun `login - when credentials are valid - should return token`() {
        val login = "testuser"
        val password = "password"
        val loginRequest = LoginRequest(login, password)
        
        val mockUser = User(
            id = 1L,
            name = "Test",
            surname = "User",
            login = login,
            passwordInternal = "encoded_password"
        )
        
        val mockAuthentication = mock(Authentication::class.java)
        `when`(mockAuthentication.principal).thenReturn(mockUser)
        
        `when`(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken::class.java)))
            .thenReturn(mockAuthentication)
            
        val mockToken = "jwt.token.here"
        `when`(jwtTokenProvider.createToken(login, mockUser.id)).thenReturn(mockToken)
        
        val result = authController.login(loginRequest)
        
        assertTrue(result is ResponseEntity<*>)
        assertEquals(HttpStatus.OK, result.statusCode)
        
        val jwtResponse = result.body as JwtResponse
        assertEquals(mockToken, jwtResponse.token)
        assertEquals(mockUser.id, jwtResponse.id)
        assertEquals(mockUser.login, jwtResponse.login)
        assertEquals(mockUser.name, jwtResponse.name)
        assertEquals(mockUser.surname, jwtResponse.surname)
    }
    
    @Test
    fun `login - when credentials are invalid - should return unauthorized`() {
        val loginRequest = LoginRequest("invaliduser", "wrongpassword")

        `when`(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken::class.java)))
            .thenThrow(org.springframework.security.authentication.BadCredentialsException("Bad credentials"))
        
        val result = authController.login(loginRequest)
        
        assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)
        assertEquals("Invalid login or password", result.body)
    }
    
    @Test
    fun `register - when login is unique - should create user`() {
        val registerRequest = RegisterRequest(
            name = "John",
            surname = "Doe",
            login = "johndoe",
            password = "password"
        )
        
        `when`(userRepository.findByLogin(registerRequest.login)).thenReturn(Optional.empty())
        `when`(passwordEncoder.encode(registerRequest.password)).thenReturn("encoded_password")
        
        val savedUser = User(
            id = 1L,
            name = registerRequest.name,
            surname = registerRequest.surname,
            login = registerRequest.login,
            passwordInternal = "encoded_password"
        )
        
        `when`(userRepository.save(any(User::class.java))).thenReturn(savedUser)
        
        val result = authController.register(registerRequest)
        
        assertEquals(HttpStatus.CREATED, result.statusCode)
        val responseMap = result.body as Map<*, *>
        assertEquals(savedUser.id, responseMap["id"])
        assertEquals(savedUser.name, responseMap["name"])
        assertEquals(savedUser.surname, responseMap["surname"])
        assertEquals(savedUser.login, responseMap["login"])
    }
    
    @Test
    fun `register - when login is taken - should return bad request`() {
        val registerRequest = RegisterRequest(
            name = "John",
            surname = "Doe",
            login = "existinguser",
            password = "password"
        )
        
        val existingUser = User(
            id = 1L,
            name = "Existing",
            surname = "User",
            login = registerRequest.login,
            passwordInternal = "already_encoded_password"
        )
        
        `when`(userRepository.findByLogin(registerRequest.login)).thenReturn(Optional.of(existingUser))
        
        val result = authController.register(registerRequest)

        assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        assertEquals("Login is already taken!", result.body)
    }
    
    @Test
    fun `getCurrentUser - should return user details`() {
        val user = User(
            id = 1L,
            name = "Test",
            surname = "User",
            login = "testuser",
            passwordInternal = "encoded_password"
        )

        val result = authController.getCurrentUser(user)
        
        assertEquals(HttpStatus.OK, result.statusCode)
        val responseMap = result.body as Map<*, *>
        assertEquals(user.id, responseMap["id"])
        assertEquals(user.name, responseMap["name"])
        assertEquals(user.surname, responseMap["surname"])
        assertEquals(user.login, responseMap["login"])
    }
}
