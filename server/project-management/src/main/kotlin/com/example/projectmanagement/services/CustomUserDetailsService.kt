package com.example.projectmanagement.services

import com.example.projectmanagement.repositories.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails { // username здесь - это login
        return userRepository.findByLogin(username)
            .orElseThrow { UsernameNotFoundException("User not found with login: $username") }
    }
}
