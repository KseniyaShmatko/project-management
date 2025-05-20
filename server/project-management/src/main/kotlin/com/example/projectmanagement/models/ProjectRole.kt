// Где-нибудь в com.example.projectmanagement.models или в отдельном файле
package com.example.projectmanagement.models

enum class ProjectRole {
    OWNER,  // Исключительные права, управление участниками
    EDITOR, // Права на запись контента проекта
    VIEWER  // Только чтение
}
