// src/main/kotlin/com/example/projectmanagement/controllers/AuthController.kt
package com.example.projectmanagement.controllers

import com.example.projectmanagement.controllers.dto.JwtResponse
import com.example.projectmanagement.controllers.dto.LoginRequest
import com.example.projectmanagement.controllers.dto.RegisterRequest
import com.example.projectmanagement.models.User
import com.example.projectmanagement.repositories.UserRepository
import com.example.projectmanagement.security.JwtTokenProvider
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.annotation.AuthenticationPrincipal

@RestController
@RequestMapping("/users") // Базовый путь для всех эндпоинтов в этом контроллере
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    // Эндпоинт для входа теперь будет /users/login
    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<*> { // Убрал <Any> для краткости, т.к. * уже это покрывает
        return try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.login, loginRequest.password)
            )
            SecurityContextHolder.getContext().authentication = authentication
            val user = authentication.principal as User
            val token = jwtTokenProvider.createToken(user.login, user.id)
            ResponseEntity.ok(JwtResponse(
                token = token,
                id = user.id,
                login = user.login,
                name = user.name,
                surname = user.surname,
                photo = user.photo
            ))
        } catch (e: AuthenticationException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid login or password")
        }
    }

    // Эндпоинт для регистрации /users/register (совпадает с SecurityConfig)
    @PostMapping("/register")
    fun register(@RequestBody registerRequest: RegisterRequest): ResponseEntity<*> {
        if (userRepository.findByLogin(registerRequest.login).isPresent) {
            return ResponseEntity.badRequest().body("Login is already taken!")
        }

        val user = User(
            name = registerRequest.name,
            surname = registerRequest.surname,
            login = registerRequest.login,
            // ИЗМЕНЕНИЕ ЗДЕСЬ: параметр "password" заменен на "passwordInternal"
            passwordInternal = passwordEncoder.encode(registerRequest.password),
            photo = registerRequest.photo
            // enabledInternal, accountNonExpiredInternal и т.д. получат свои значения по умолчанию
            // из конструктора data class User, их здесь указывать не нужно, если значения по умолчанию подходят.
        )
        val savedUser = userRepository.save(user)

        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapOf(
                "id" to savedUser.id,
                "name" to savedUser.name,
                "surname" to savedUser.surname,
                "login" to savedUser.login,
                "photo" to savedUser.photo
            )
        )
    }

    // Эндпоинт для получения данных текущего пользователя /users/me
    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal userDetails: User): ResponseEntity<Any> {
        return ResponseEntity.ok(mapOf(
            "id" to userDetails.id,
            "name" to userDetails.name,
            "surname" to userDetails.surname,
            "login" to userDetails.login, // Это username
            "photo" to userDetails.photo
            // Для отладки можно посмотреть authorities:
            // "authorities" to userDetails.authorities.map { it.authority }
        ))
    }
}
