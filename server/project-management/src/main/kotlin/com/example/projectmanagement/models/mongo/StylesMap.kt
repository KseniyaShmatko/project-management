package com.example.projectmanagement.models.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "styles_map")
data class StylesMap(
    @Id
    var id: String? = null,
    var objectType: String? = "styles_map",
    var links: List<StyleLink>? = null
)

data class StyleLink(
    var elementId: String? = null,
    var styleId: String? = null,
    var start: Int? = null,
    var end: Int? = null
)
