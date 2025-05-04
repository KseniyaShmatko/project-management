package com.example.projectmanagement.models

import jakarta.persistence.*

@Entity
@Table(name = "projects_files")
data class ProjectFile(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    val project: Project,

    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    val file: File
)
