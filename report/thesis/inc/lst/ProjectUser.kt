@Entity
@Table(name = "projects_users")
data class ProjectUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    val project_id: Project,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user_id: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: ProjectRole
)
