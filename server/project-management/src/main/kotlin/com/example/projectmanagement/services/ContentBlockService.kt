package com.example.projectmanagement.services

import com.example.projectmanagement.models.mongo.ContentBlock
import com.example.projectmanagement.repositories.mongo.ContentBlockRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.NoSuchElementException

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
        val createdBlock = repo.save(block)
        return createdBlock
    }

    fun getById(id: String): ContentBlock {
        val block = repo.findById(id)
            .orElseThrow { 
                NoSuchElementException("ContentBlock with id '$id' not found") 
            }
        return block
    }

    fun update(id: String, updates: ContentBlock): ContentBlock {
        val existingBlock = repo.findById(id)
            .orElseThrow { 
                NoSuchElementException("ContentBlock with id '$id' not found for update") 
            }
        
        if (updates.data != null) {
            existingBlock.data = updates.data
        }
        if (updates.label != null) {
            existingBlock.label = updates.label
        }
        if (updates.items != null) {
            existingBlock.items = updates.items
        }
        if (updates.marker != null) {
            existingBlock.marker = updates.marker
        }
        if (updates.position != null) {
            existingBlock.position = updates.position
        }

        val updatedBlock = repo.save(existingBlock)
        return updatedBlock
    }

    fun delete(id: String) {
        if (repo.existsById(id)) {
            repo.deleteById(id)
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "ContentBlock with id '$id' not found for deletion")
        }
    }
}
