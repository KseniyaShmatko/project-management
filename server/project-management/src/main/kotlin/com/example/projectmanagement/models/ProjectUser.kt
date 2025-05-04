package com.example.projectmanagement.models

import jakarta.persistence.*

@Entity
@Table(name = "projects_users")
data class ProjectUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    val project: Project,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    var role: String,
    var permission: String
)
