package com.example.projectmanagement.services

import com.example.projectmanagement.models.mongo.SuperObject
import com.example.projectmanagement.repositories.mongo.SuperObjectRepository
import org.springframework.stereotype.Service

@Service
class SuperObjectService(
    private val repo: SuperObjectRepository
) {
    fun create(s: SuperObject): SuperObject {
        val createdSO = repo.save(s)
        return createdSO
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
}
