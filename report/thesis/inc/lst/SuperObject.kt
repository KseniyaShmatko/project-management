@Document(collection = "super_objects")
data class SuperObject(
    @Id
    var id: String? = null,
    @Indexed(unique = true)
    var fileId: Long? = null,
    var serviceType: String? = null,
    var lastChangeDate: String? = null,
    var name: String? = null,
    var firstItem: String? = null,
    var lastItem: String? = null,
    var stylesMapId: String? = null
)
