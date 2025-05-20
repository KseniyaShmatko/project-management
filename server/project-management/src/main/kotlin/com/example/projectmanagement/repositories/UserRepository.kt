package com.example.projectmanagement.repositories

import com.example.projectmanagement.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByLogin(login: String): Optional<User>
    fun existsByLogin(login: String): Boolean
    fun findByLoginContainingIgnoreCase(login: String): List<User>
}