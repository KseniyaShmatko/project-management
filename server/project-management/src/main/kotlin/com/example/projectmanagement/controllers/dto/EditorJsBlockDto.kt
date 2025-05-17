package com.example.projectmanagement.controllers.dto

// DTO для представления данных одного блока от Editor.js
data class EditorJsBlockDto(
    val id: String?, // ID блока, который генерирует Editor.js (может быть новым или существующим)
    val type: String?,  // Тип блока (e.g., "paragraph", "header", "list")
    val data: Map<String, Any> // Данные блока
    // tunes: Map<String, Any>? // Если используешь "настройки" блока (tunes) в Editor.js
)
