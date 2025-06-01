@Document(collection = "task_column")
data class TaskColumn(
    @Id
    var id: String? = null,
    var superObjectId: String? = null,
    var name: String? = null,
    var order: Int? = null
)
