@Document(collection = "content_blocks")
data class ContentBlock(
    @Id
    var id: String? = null,
    var objectType: String? = null,
    
    var data: Map<String, Any>? = null,

    var nextItem: String? = null, 
    var prevItem: String? = null,
)
