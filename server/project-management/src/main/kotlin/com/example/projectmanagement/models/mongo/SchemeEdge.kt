package com.example.projectmanagement.models.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "scheme_edge")
data class SchemeEdge(
    @Id
    var id: String? = null,
    var superObjectId: String? = null,
    var type: String? = null,
    var sourceNodeId: String? = null,
    var targetNodeId: String? = null,
    var data: Map<String, Any>? = null
)
