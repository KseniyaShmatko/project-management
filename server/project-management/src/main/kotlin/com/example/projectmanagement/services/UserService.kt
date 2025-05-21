package com.example.projectmanagement.services

import com.example.projectmanagement.models.User
import com.example.projectmanagement.repositories.UserRepository
import com.example.projectmanagement.controllers.dto.UserResponseDto
import org.springframework.stereotype.Service
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(private val userRepository: UserRepository) {

    fun getUserById(id: Long): User {
        return userRepository.findById(id).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }
    }

    fun updateUser(id: Long, user: User): User {
        if (userRepository.existsById(id)) {
            val existingUser = userRepository.findById(id).get()
            val updatedUser = existingUser.copy(
                name = user.name,
                surname = user.surname,
                photo = user.photo ?: existingUser.photo
            )
            return userRepository.save(updatedUser)
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }
    }

    fun deleteUser(id: Long) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id)
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        }
    }

    private fun User.toUserResponseDto(): UserResponseDto = UserResponseDto(this.id, this.name, this.surname, this.login, this.photo)

    fun searchUsersByLogin(loginQuery: String): List<UserResponseDto> {
        return userRepository.findByLoginContainingIgnoreCase(loginQuery)
            .map { it.toUserResponseDto() }
    }
}
