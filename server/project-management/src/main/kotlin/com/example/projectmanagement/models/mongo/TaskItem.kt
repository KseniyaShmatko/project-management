package com.example.projectmanagement.models.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "task_item")
data class TaskItem(
    @Id
    var id: String? = null,
    var superObjectId: String? = null,
    var columnId: String? = null,
    var title: String? = null,
    var description: String? = null,
    var orderInColumn: Int? = null,
    var assigneeId: Int? = null,
    var reporterId: Int? = null,
    var priority: String? = null,
    var dueDate: Date? = null,
    var tags: List<String>? = null,
    var attachments: List<Int>? = null,
    var subtasks: List<String>? = null
)
