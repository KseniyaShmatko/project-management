package com.example.projectmanagement.models.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "content_blocks")
data class ContentBlock(
    @Id
    var id: String? = null,
    var objectType: String? = null, // "text", "picture", "list"

    // общие поля для всех блоков
    var nextItem: String? = null,
    var prevItem: String? = null,

    // для текста и картинок
    var data: String? = null,

    // для картинок
    var label: String? = null,

    // для списка
    var items: List<String>? = null, // id элементов
    var marker: String? = null,
    var position: Any? = null, 
)
