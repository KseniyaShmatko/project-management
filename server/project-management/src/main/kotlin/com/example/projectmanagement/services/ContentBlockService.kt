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
            println("ContentBlockService: Block with client-provided id '${block.id}' already exists. Consider update.")
            throw ResponseStatusException(HttpStatus.CONFLICT, "...") 
        }
        val createdBlock = repo.save(block)
        println("ContentBlockService: Created/Saved block: $createdBlock")
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
            .orElseThrow { NoSuchElementException("ContentBlock with id '$id' not found for update") }
        
        updates.objectType?.let { existingBlock.objectType = it }
        updates.data?.let { existingBlock.data = it }

        return repo.save(existingBlock)
    }
    
    fun delete(id: String) {
        if (repo.existsById(id)) {
            repo.deleteById(id)
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "ContentBlock with id '$id' not found for deletion")
        }
    }
}
