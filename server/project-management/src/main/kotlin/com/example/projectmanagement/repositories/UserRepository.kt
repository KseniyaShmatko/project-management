package com.example.projectmanagement.repositories

import com.example.projectmanagement.models.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>
