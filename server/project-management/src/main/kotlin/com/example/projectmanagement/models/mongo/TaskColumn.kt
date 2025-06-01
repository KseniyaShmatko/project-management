package com.example.projectmanagement.models.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "task_column")
data class TaskColumn(
    @Id
    var id: String? = null,
    var superObjectId: String? = null,
    var name: String? = null,
    var order: Int? = null
)
