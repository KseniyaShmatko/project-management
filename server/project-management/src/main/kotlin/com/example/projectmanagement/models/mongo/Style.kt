package com.example.projectmanagement.models.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "styles")
data class Style(
    @Id
    var id: String? = null,
    var objectType: String? = "style",
    var appliedObject: String? = null, // "text" | "picture" | "video"
    var alignment: String? = null,
    var color: String? = null,
    var fontFamily: String? = null,
    var fontSize: Int? = null,
    var marginTop: Int? = null,
    var marginRight: Int? = null,
    var marginLeft: Int? = null,
    var marginBottom: Int? = null,
    var frame: Boolean? = null,
    var padding: List<Int>? = null,       // [top, right, bottom, left]
    var position: List<Int>? = null       // [top, right, bottom, left]
)
