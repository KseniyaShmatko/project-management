package com.example.projectmanagement.repositories

import com.example.projectmanagement.models.File
import com.example.projectmanagement.models.FileType
import org.springframework.data.jpa.repository.JpaRepository

interface FileRepository : JpaRepository<File, Long>

interface FileTypeRepository : JpaRepository<FileType, Long> {
    fun findByName(name: String): FileType?
}