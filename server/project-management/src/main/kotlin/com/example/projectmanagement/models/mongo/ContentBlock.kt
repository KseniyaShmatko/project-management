package com.example.projectmanagement.models.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "content_blocks")
data class ContentBlock(
    @Id
    var id: String? = null,
    var objectType: String? = null,

    var nextItem: String? = null, 
    var prevItem: String? = null,

    var data: Map<String, Any>? = null,
)
