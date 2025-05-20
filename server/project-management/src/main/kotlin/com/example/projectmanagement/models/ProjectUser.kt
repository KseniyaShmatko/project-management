package com.example.projectmanagement.models

import jakarta.persistence.*

@Entity
@Table(name = "projects_users")
data class ProjectUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false) // Добавил nullable = false
    val project: Project,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Добавил nullable = false
    val user: User,

    @Enumerated(EnumType.STRING) // Хранить как строку (OWNER, EDITOR, VIEWER)
    @Column(nullable = false)    // Роль обязательна
    var role: ProjectRole
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as ProjectUser
        // Для связующих таблиц можно сравнивать по комбинации внешних ключей, если ID самой записи не так важен,
        // ИЛИ просто по id, если он уникален и используется.
        // У нас есть id, так что используем его.
        return if (id == 0L) false else id == other.id
        // Альтернатива (если бы не было своего id, а уникальна пара project+user):
        // return project.id == other.project.id && user.id == other.user.id
    }

    override fun hashCode(): Int {
        return if (id != 0L) id.hashCode() else System.identityHashCode(this)
        // Альтернатива:
        // return Objects.hash(project.id, user.id)
    }

    override fun toString(): String {
        return "ProjectUser(id=$id, projectId=${project.id}, userId=${user.id}, role=$role)"
    }
}
