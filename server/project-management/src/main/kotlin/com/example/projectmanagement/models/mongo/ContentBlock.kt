package com.example.projectmanagement.models.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "content_blocks")
data class ContentBlock(
    @Id
    var id: String? = null, // Editor.js тоже генерирует ID для блоков, можно использовать его или свой
    var objectType: String? = null, // Это будет тип блока из Editor.js (e.g., "paragraph", "header", "image", "list")

    var nextItem: String? = null, // Для связности твоих блоков
    var prevItem: String? = null, // Для связности твоих блоков

    // Это поле станет КЛЮЧЕВЫМ. Оно будет хранить объект 'data' от блока Editor.js,
    // сериализованный в JSON-строку, или ты можешь использовать Map<String, Any>
    var data: Map<String, Any>? = null, // ИЛИ Map<String, Any>? ИЛИ String? (для JSON-строки)
)
