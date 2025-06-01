@Document(collection = "scheme_node")
data class SchemeNode(
    @Id
    var id: String? = null,
    var superObjectId: String? = null,
    var type: String? = null,
    var x: Int? = null,
    var y: Int? = null,
    var width: Int? = null,
    var height: Int? = null,
    var data: Map<String, Any>? = null
)
