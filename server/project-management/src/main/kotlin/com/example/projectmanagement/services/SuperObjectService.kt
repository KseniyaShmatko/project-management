package com.example.projectmanagement.services

import com.example.projectmanagement.models.mongo.SuperObject
import com.example.projectmanagement.models.mongo.ContentBlock
import com.example.projectmanagement.repositories.mongo.SuperObjectRepository
import com.example.projectmanagement.repositories.mongo.ContentBlockRepository
import com.example.projectmanagement.controllers.dto.EditorJsBlockDto
import org.springframework.transaction.annotation.Transactional 
import org.springframework.dao.DuplicateKeyException 
import org.springframework.stereotype.Service
import java.util.NoSuchElementException

@Service
class SuperObjectService(
    private val repo: SuperObjectRepository,
    private val contentBlockRepo: ContentBlockRepository
) {

    fun create(soToCreate: SuperObject): SuperObject {
        println("Backend SuperObjectService: Attempting to create SO with fileId: ${soToCreate.fileId}")
        if (soToCreate.fileId == null) {
            throw IllegalArgumentException("fileId must be provided to create SuperObject")
        }

        try {
            println("Backend SuperObjectService: Attempting to save new SO: $soToCreate")
            val createdSO = repo.save(soToCreate)
            println("Backend SuperObjectService: Successfully saved new SO: $createdSO")
            return createdSO
        } catch (e: DuplicateKeyException) {
            println("Backend SuperObjectService: Caught DuplicateKeyException for fileId ${soToCreate.fileId}. Re-fetching.")
            val existingSO = repo.findByFileId(soToCreate.fileId!!)
                ?: throw IllegalStateException("SuperObject with fileId ${soToCreate.fileId} should exist after DuplicateKeyException but not found.")
            println("Backend SuperObjectService: Found existing SO after DuplicateKeyException: $existingSO")
            return existingSO
        }
    }

    fun getByFileId(fileId: Long): SuperObject? {
        val foundSO = repo.findByFileId(fileId)
        return foundSO
    }

    fun getById(id: String): SuperObject = repo.findById(id).orElseThrow { NoSuchElementException("Not found") }

    fun update(id: String, updates: SuperObject): SuperObject {
        val existingSO = repo.findById(id)
            .orElseThrow { NoSuchElementException("SuperObject with id '$id' not found for update") }

        updates.name?.let { existingSO.name = it }
        updates.serviceType?.let { existingSO.serviceType = it }
        updates.lastChangeDate?.let { existingSO.lastChangeDate = it }
        updates.template?.let { existingSO.template = it }
        updates.decoration?.let { existingSO.decoration = it }
        updates.firstItem?.let { existingSO.firstItem = it }
        updates.lastItem?.let { existingSO.lastItem = it }
        updates.checkSum?.let { existingSO.checkSum = it }
        updates.stylesMapId?.let { existingSO.stylesMapId = it }

        val updatedSO = repo.save(existingSO)
        return updatedSO
    }
    
    fun delete(id: String) = repo.deleteById(id)

    fun updateMetadata(id: String, updates: SuperObject): SuperObject {
        val existingSO = repo.findById(id)
            .orElseThrow { NoSuchElementException("SuperObject with id '$id' not found for update") }

        updates.name?.let { existingSO.name = it }
        updates.serviceType?.let { existingSO.serviceType = it }
        updates.lastChangeDate?.let { existingSO.lastChangeDate = it }
        updates.template?.let { existingSO.template = it }
        updates.decoration?.let { existingSO.decoration = it }
        
        return repo.save(existingSO)
    }


    @Transactional
    fun syncDocumentBlocks(superObjectId: String, editorJsBlocksData: List<EditorJsBlockDto>): SuperObject {
        val superObject = repo.findById(superObjectId)
            .orElseThrow { NoSuchElementException("SuperObject with id '$superObjectId' not found") }

        val existingBlockIdsInDb = mutableSetOf<String>()
        var tempCurrentBlockId = superObject.firstItem
        while(tempCurrentBlockId != null) {
            existingBlockIdsInDb.add(tempCurrentBlockId)
            val cb = contentBlockRepo.findById(tempCurrentBlockId).orElse(null)
            tempCurrentBlockId = cb?.nextItem
        }
        val newBlockClientIds = editorJsBlocksData.mapNotNull { it.id }.toSet() 

        val blocksToRemove = existingBlockIdsInDb.filterNot { existingId ->
            newBlockClientIds.contains(existingId) || editorJsBlocksData.any { it.id == existingId }
        }
        if (blocksToRemove.isNotEmpty()) {
            contentBlockRepo.deleteAllById(blocksToRemove) 
            println("Removed blocks: $blocksToRemove")
        }


        var previousSavedBlockEntity: ContentBlock? = null
        val finalBlockOrderIds = mutableListOf<String>()

        for ((index, editorBlockData) in editorJsBlocksData.withIndex()) {
            val currentBlockEntity: ContentBlock

            if (editorBlockData.id != null && contentBlockRepo.existsById(editorBlockData.id!!)) {
                currentBlockEntity = contentBlockRepo.findById(editorBlockData.id!!).get()
                currentBlockEntity.objectType = editorBlockData.type
                currentBlockEntity.data = editorBlockData.data
                 println("Updating existing block: ${currentBlockEntity.id}")
            } else {
                currentBlockEntity = ContentBlock(
                    id = editorBlockData.id,
                    objectType = editorBlockData.type,
                    data = editorBlockData.data
                )
                println("Creating new block (client ID: ${editorBlockData.id})")
            }

            currentBlockEntity.prevItem = previousSavedBlockEntity?.id
            currentBlockEntity.nextItem = null

            val savedCurrentBlock = contentBlockRepo.save(currentBlockEntity)
            finalBlockOrderIds.add(savedCurrentBlock.id!!)
            println("Saved block: ${savedCurrentBlock.id}, prev: ${savedCurrentBlock.prevItem}")

            if (previousSavedBlockEntity != null) {
                previousSavedBlockEntity.nextItem = savedCurrentBlock.id
                contentBlockRepo.save(previousSavedBlockEntity)
                println("Updated PREVIOUS block ${previousSavedBlockEntity.id} with nextItem: ${savedCurrentBlock.id}")
            }

            previousSavedBlockEntity = savedCurrentBlock
        }

        superObject.firstItem = finalBlockOrderIds.firstOrNull()
        superObject.lastItem = finalBlockOrderIds.lastOrNull()
        superObject.lastChangeDate = java.time.OffsetDateTime.now().toString()

        return repo.save(superObject)
    }
}
