package com.example.projectmanagement.services

import com.example.projectmanagement.models.mongo.Style
import com.example.projectmanagement.repositories.mongo.StyleRepository
import org.springframework.stereotype.Service

@Service
class StyleService(
    private val repo: StyleRepository
) {
    fun create(style: Style): Style = repo.save(style)

    fun getById(id: String): Style = repo.findById(id).orElseThrow { NoSuchElementException("Not found") }
    
    fun update(id: String, style: Style): Style { style.id = id; return repo.save(style) }

    fun delete(id: String) = repo.deleteById(id)
}
