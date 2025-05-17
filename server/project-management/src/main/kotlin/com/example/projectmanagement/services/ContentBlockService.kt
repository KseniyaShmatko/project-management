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
        // Если ID приходит от Editor.js и мы хотим его использовать:
        if (block.id != null && repo.existsById(block.id!!)) {
            // Возможно, здесь стоит обновить существующий, а не бросать конфликт,
            // или пусть это обрабатывает syncDocumentBlocks
            println("ContentBlockService: Block with client-provided id '${block.id}' already exists. Consider update.")
            // Для идемпотентности можно обновить:
            // return update(block.id!!, block)
            // Пока оставим Conflict для явности, если этот метод вызывается напрямую
            throw ResponseStatusException(HttpStatus.CONFLICT, "...") 
        }
        // Если block.id == null, MongoDB сгенерирует ID
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
        
        // Обновляем только то, что могло прийти от Editor.js или для чего есть смысл
        updates.objectType?.let { existingBlock.objectType = it }
        updates.data?.let { existingBlock.data = it } // data это Map<String, Any>
        
        // prevItem и nextItem обычно этим методом не должны меняться,
        // они устанавливаются при пакетном сохранении
        // updates.prevItem?.let { existingBlock.prevItem = it } // Осторожно!
        // updates.nextItem?.let { existingBlock.nextItem = it } // Осторожно!

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
