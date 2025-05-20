package com.example.projectmanagement.models.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.index.Indexed

@Document(collection = "super_objects")
data class SuperObject(
    @Id
    var id: String? = null,
    @Indexed(unique = true)
    var fileId: Long? = null,
    var serviceType: String? = null,
    var lastChangeDate: String? = null,
    var name: String? = null,
    var template: Template? = null,
    var decoration: Decoration? = null,
    var firstItem: String? = null,
    var lastItem: String? = null,
    var checkSum: Long? = null,
    var stylesMapId: String? = null
)

data class Template(
    var image: String? = null,
    var type: String? = null,
    var color: String? = null
)

data class Decoration(
    var marginTop: Int? = null,
    var marginRight: Int? = null,
    var marginLeft: Int? = null,
    var marginBottom: Int? = null
)
