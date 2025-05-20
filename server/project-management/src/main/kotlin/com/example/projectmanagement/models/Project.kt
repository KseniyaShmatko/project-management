package com.example.projectmanagement.models

import jakarta.persistence.*
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@NamedEntityGraph(
    name = "Project.withDetails",
    attributeNodes = [
        NamedAttributeNode("owner"), // Загружаем владельца
        NamedAttributeNode(value = "projectUsers", subgraph = "projectUsers-subgraph"), // Загружаем участников и их пользователей
        NamedAttributeNode(value = "projectFiles", subgraph = "projectFiles-subgraph")  // Загружаем файлы проекта и их детали
    ],
    subgraphs = [
        NamedSubgraph(
            name = "projectUsers-subgraph",
            attributeNodes = [NamedAttributeNode("user")] // Внутри ProjectUser загружаем User
        ),
        NamedSubgraph(
            name = "projectFiles-subgraph",
            attributeNodes = [
                NamedAttributeNode(value = "file", subgraph = "file-subgraph") // Внутри ProjectFile загружаем File
            ]
        ),
        NamedSubgraph( // Отдельный субграф для деталей File, так как он используется в projectFiles-subgraph
            name = "file-subgraph",
            attributeNodes = [NamedAttributeNode("type")] // Внутри File загружаем FileType
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
        // Если id == 0, то это новая, еще не сохраненная сущность.
        // Сравнивать новые сущности по id (который 0) не имеет смысла,
        // они будут равны только если это один и тот же объект (this === other).
        // Если id != 0, то это уже сохраненная сущность, и ее можно сравнивать по id.
        return if (id == 0L) false else id == other.id
    }

    override fun hashCode(): Int {
        // Используем id для хэш-кода, если он не 0.
        // Для новых сущностей (id == 0), можно вернуть константу или this.javaClass.hashCode(),
        // чтобы избежать коллизий, если они добавляются в Set до сохранения.
        return if (id != 0L) id.hashCode() else System.identityHashCode(this)
        // Или просто:
        // return id.hashCode() // Если уверены, что в Set/Map попадают только сохраненные сущности
    }

    override fun toString(): String {
        return "Project(id=$id, name='$name', description=$description, ownerId=${owner?.id})"
    }
}