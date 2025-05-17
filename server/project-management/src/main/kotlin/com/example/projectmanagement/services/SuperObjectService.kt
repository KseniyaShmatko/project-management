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
            // Сразу пытаемся сохранить. Если уникальный индекс есть, и такой fileId уже существует,
            // этот вызов бросит DuplicateKeyException.
            println("Backend SuperObjectService: Attempting to save new SO: $soToCreate")
            val createdSO = repo.save(soToCreate) // repo это SuperObjectRepository
            println("Backend SuperObjectService: Successfully saved new SO: $createdSO")
            return createdSO
        } catch (e: DuplicateKeyException) {
            // Если объект с таким fileId уже существует (сработал уникальный индекс)
            println("Backend SuperObjectService: Caught DuplicateKeyException for fileId ${soToCreate.fileId}. Re-fetching.")
            // Делаем повторный поиск, так как объект теперь точно должен быть там
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
        // firstItem, lastItem, checkSum, stylesMapId - будут управляться syncDocumentBlocks
        
        return repo.save(existingSO)
    }


    @Transactional // Попытка сделать операцию "атомарной" (с ограничениями MongoDB)
    fun syncDocumentBlocks(superObjectId: String, editorJsBlocksData: List<EditorJsBlockDto>): SuperObject {
        val superObject = repo.findById(superObjectId)
            .orElseThrow { NoSuchElementException("SuperObject with id '$superObjectId' not found") }

        // 1. Собираем ID существующих блоков, чтобы найти удаленные
        val existingBlockIds = mutableSetOf<String>()
        var currentBlockId = superObject.firstItem
        while(currentBlockId != null) {
            existingBlockIds.add(currentBlockId)
            val cb = contentBlockRepo.findById(currentBlockId).orElse(null)
            currentBlockId = cb?.nextItem
        }

        val newBlockIds = editorJsBlocksData.mapNotNull { it.id }.toSet()
        val blocksToRemove = existingBlockIds.filterNot { newBlockIds.contains(it) }
        contentBlockRepo.deleteAllById(blocksToRemove) // Удаляем блоки, которых нет в новом списке

        val savedContentBlocks = mutableListOf<ContentBlock>()
        var previousBlockMongoId: String? = null

        for (editorBlockData in editorJsBlocksData) {
            val contentBlockToSave: ContentBlock
            // Пытаемся найти и обновить существующий блок по ID от Editor.js, если он есть
            if (editorBlockData.id != null && contentBlockRepo.existsById(editorBlockData.id)) {
                contentBlockToSave = contentBlockRepo.findById(editorBlockData.id).get()
                contentBlockToSave.objectType = editorBlockData.type
                contentBlockToSave.data = editorBlockData.data
            } else {
                // Создаем новый блок, если ID нет или по такому ID блок не найден
                contentBlockToSave = ContentBlock(
                    // id будет сгенерирован MongoDB, если editorBlockData.id null или такого нет
                    // или можно использовать editorBlockData.id, если он уникален и ты хочешь его сохранить
                    id = editorBlockData.id, // Если Editor.js генерирует свои ID, и ты хочешь их сохранить
                    objectType = editorBlockData.type,
                    data = editorBlockData.data
                )
            }
            
            contentBlockToSave.prevItem = previousBlockMongoId
            contentBlockToSave.nextItem = null // Будет установлено на следующей итерации, если есть след. блок

            val savedBlock = contentBlockRepo.save(contentBlockToSave)
            savedContentBlocks.add(savedBlock)

            if (previousBlockMongoId != null) {
                // Обновляем nextItem у предыдущего сохраненного блока
                val prevSavedBlock = savedContentBlocks.find { it.id == previousBlockMongoId } // или загрузить из repo
                prevSavedBlock?.nextItem = savedBlock.id
                prevSavedBlock?.let { contentBlockRepo.save(it) }
            }
            previousBlockMongoId = savedBlock.id
        }

        superObject.firstItem = savedContentBlocks.firstOrNull()?.id
        superObject.lastItem = savedContentBlocks.lastOrNull()?.id
        superObject.lastChangeDate = java.time.OffsetDateTime.now().toString() // Обновляем дату

        return repo.save(superObject)
    }
}
