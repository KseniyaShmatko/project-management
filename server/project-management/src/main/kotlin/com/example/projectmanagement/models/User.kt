// diploma/server/project-management/src/main/kotlin/com/example/projectmanagement/models/User.kt
package com.example.projectmanagement.models

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.Objects

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var name: String,
    var surname: String,

    // Это поле будет использоваться UserRepository.findByLogin()
    // и будет возвращаться методом getUsername()
    @Column(unique = true, nullable = false)
    var login: String,

    // Переименовываем поле, чтобы избежать конфликта с методом getPassword()
    @Column(nullable = false)
    var passwordInternal: String,

    var photo: String? = null,

    // Переименовываем поля для UserDetails boolean-флагов
    var enabledInternal: Boolean = true,
    var accountNonExpiredInternal: Boolean = true,
    var accountNonLockedInternal: Boolean = true,
    var credentialsNonExpiredInternal: Boolean = true

    // Важно: убедитесь, что ваш build.gradle.kts сконфигурирован для плагина kotlin-jpa
    // (обычно через allOpen { annotation("jakarta.persistence.Entity") }).
    // Этот плагин генерирует конструктор без аргументов для JPA-сущностей.
    // Если плагина нет или он настроен неверно, JPA может потребовать явный конструктор без аргументов:
    // constructor() : this(0, "", "", "", "", null, true, true, true, true) // Пример

) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList()
    override fun getPassword(): String = this.passwordInternal
    override fun getUsername(): String = this.login
    override fun isAccountNonExpired(): Boolean = this.accountNonExpiredInternal
    override fun isAccountNonLocked(): Boolean = this.accountNonLockedInternal
    override fun isCredentialsNonExpired(): Boolean = this.credentialsNonExpiredInternal
    override fun isEnabled(): Boolean = this.enabledInternal

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as User
        return if (id == 0L) false else id == other.id
    }

    override fun hashCode(): Int {
        return if (id != 0L) id.hashCode() else System.identityHashCode(this)
    }

    override fun toString(): String {
        return "User(id=$id, login='$login', name='$name', surname='$surname')"
    }
}
