package com.example.projectmanagement.services

import com.example.projectmanagement.models.mongo.StylesMap
import com.example.projectmanagement.repositories.mongo.StylesMapRepository
import org.springframework.stereotype.Service

@Service
class StylesMapService(
    private val repo: StylesMapRepository
) {
    fun create(sm: StylesMap): StylesMap = repo.save(sm)

    fun getById(id: String): StylesMap = repo.findById(id).orElseThrow { NoSuchElementException("Not found") }

    fun update(id: String, sm: StylesMap): StylesMap { sm.id = id; return repo.save(sm) }
    
    fun delete(id: String) = repo.deleteById(id)
}
