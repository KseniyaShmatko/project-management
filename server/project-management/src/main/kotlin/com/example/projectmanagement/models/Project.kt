package com.example.projectmanagement.models

import jakarta.persistence.*
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@NamedEntityGraph(
    name = "Project.withDetails",
    attributeNodes = [
        NamedAttributeNode("owner"),
        NamedAttributeNode(value = "projectUsers", subgraph = "projectUsers-subgraph"),
        NamedAttributeNode(value = "projectFiles", subgraph = "projectFiles-subgraph")
    ],
    subgraphs = [
        NamedSubgraph(
            name = "projectUsers-subgraph",
            attributeNodes = [NamedAttributeNode("user")]
        ),
        NamedSubgraph(
            name = "projectFiles-subgraph",
            attributeNodes = [
                NamedAttributeNode(value = "file", subgraph = "file-subgraph")
            ]
        ),
        NamedSubgraph(
            name = "file-subgraph",
            attributeNodes = [NamedAttributeNode("type")]
        )
    ]
)
@Entity
@Table(name = "projects")
data class Project(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var name: String,
    var description: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var owner: User? = null,

    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var projectUsers: MutableSet<ProjectUser> = mutableSetOf(),

    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var projectFiles: MutableSet<ProjectFile> = mutableSetOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as Project
        return if (id == 0L) false else id == other.id
    }

    override fun hashCode(): Int {
        return if (id != 0L) id.hashCode() else System.identityHashCode(this)
    }

    override fun toString(): String {
        return "Project(id=$id, name='$name', description=$description, ownerId=${owner?.id})"
    }
}