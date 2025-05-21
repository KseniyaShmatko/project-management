package com.example.projectmanagement.repositories

import com.example.projectmanagement.models.Project
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.jpa.repository.EntityGraph
import java.util.Optional
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectRepository : JpaRepository<Project, Long> {

    @EntityGraph(value = "Project.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT p FROM Project p WHERE p.owner.id = :userId")
    fun findOwnedProjectsByUserIdWithDetails(@Param("userId") userId: Long): List<Project>

    @EntityGraph(value = "Project.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT p FROM Project p WHERE EXISTS (SELECT 1 FROM ProjectUser pup WHERE pup.project.id = p.id AND pup.user.id = :userId)")
    fun findParticipatedProjectsByUserIdWithDetails(@Param("userId") userId: Long): List<Project>
    
    @EntityGraph(value = "Project.withDetails", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT p FROM Project p WHERE p.id = :projectId")
    fun findProjectByIdWithDetails(@Param("projectId") projectId: Long): Optional<Project>

    fun findByOwnerId(ownerId: Long): List<Project>
    fun existsByIdAndOwner_Id(projectId: Long, ownerId: Long): Boolean
}