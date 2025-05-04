package com.example.projectmanagement.services

import com.example.projectmanagement.models.mongo.ContentBlock
import com.example.projectmanagement.repositories.mongo.ContentBlockRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ContentBlockService(
    private val repo: ContentBlockRepository
) {
    fun create(block: ContentBlock): ContentBlock {
        if (block.id != null && repo.existsById(block.id!!)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "ContentBlock with id '${block.id}' already exists"
            )
        }
        return repo.save(block)
    }

    fun getById(id: String): ContentBlock =
        repo.findById(id).orElseThrow { NoSuchElementException("Not found") }

    fun update(id: String, block: ContentBlock): ContentBlock {
        block.id = id
        return repo.save(block)
    }

    fun delete(id: String) = repo.deleteById(id)
}
