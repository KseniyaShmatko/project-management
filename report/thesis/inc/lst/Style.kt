@Document(collection = "styles")
data class Style(
    @Id
    var id: String? = null,
    var targetType: String?,
    var attributes: Map<String, Any>? = null,
)
