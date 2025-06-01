@Entity
@Table(name = "files")
data class File(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var name: String,

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "type_id", nullable = false)
    var type_id: FileType?,

    @Column(name = "author")
    val authorId: Long,

    @Column(name = "upload_date")
    var date: LocalDateTime = LocalDateTime.now(),

    @Column(name = "super_object_id")
    var superObjectId: String? = null,
    
)

@Entity
@Table(name = "file_types")
data class FileType(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String
)