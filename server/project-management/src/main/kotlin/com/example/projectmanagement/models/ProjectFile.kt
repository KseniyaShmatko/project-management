package com.example.projectmanagement.models

import jakarta.persistence.*

@Entity
@Table(name = "projects_files")
data class ProjectFile(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY) // FetchType.LAZY здесь обычно по умолчанию для @ManyToOne, но лучше указать явно.
    @JoinColumn(name = "project_id", nullable = false)
    val project: Project,

    @ManyToOne(fetch = FetchType.LAZY) // Аналогично
    @JoinColumn(name = "file_id", nullable = false)
    val file: File
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as ProjectFile
        return if (id == 0L) false else id == other.id
    }

    override fun hashCode(): Int {
        return if (id != 0L) id.hashCode() else System.identityHashCode(this)
    }

    override fun toString(): String {
        return "ProjectFile(id=$id, projectId=${project.id}, fileId=${file.id})"
    }
}
