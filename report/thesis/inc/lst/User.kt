@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var name: String,
    var surname: String,

    @Column(unique = true, nullable = false)
    var login: String,

    @Column(nullable = false)
    var passwordInternal: String,

    var photo: String? = null,

    var enabledInternal: Boolean = true,
    var accountNonExpiredInternal: Boolean = true,
    var accountNonLockedInternal: Boolean = true,
    var credentialsNonExpiredInternal: Boolean = true
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList()
    override fun getPassword(): String = this.passwordInternal
    override fun getUsername(): String = this.login
    override fun isAccountNonExpired(): Boolean = this.accountNonExpiredInternal
    override fun isAccountNonLocked(): Boolean = this.accountNonLockedInternal
    override fun isCredentialsNonExpired(): Boolean = this.credentialsNonExpiredInternal
    override fun isEnabled(): Boolean = this.enabledInternal
}
