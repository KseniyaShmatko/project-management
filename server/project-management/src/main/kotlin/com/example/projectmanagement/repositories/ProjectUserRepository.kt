package com.example.projectmanagement.repositories

import com.example.projectmanagement.models.ProjectUser
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectUserRepository : JpaRepository<ProjectUser, Long> {
    fun findAllByUser_Id(userId: Long): List<ProjectUser>
    fun findAllByProject_Id(projectId: Long): List<ProjectUser>
    fun findByProject_IdAndUser_Id(projectId: Long, userId: Long): ProjectUser?
    fun existsByProject_IdAndUser_Id(projectId: Long, userId: Long): Boolean
}
