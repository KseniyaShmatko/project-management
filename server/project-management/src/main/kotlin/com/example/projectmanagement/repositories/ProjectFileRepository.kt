package com.example.projectmanagement.repositories

import com.example.projectmanagement.models.ProjectFile
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectFileRepository : JpaRepository<ProjectFile, Long> {
    fun findAllByProject_Id(projectId: Long): List<ProjectFile>
    fun findAllByFile_Id(fileId: Long): List<ProjectFile>
}
