package com.example.projectmanagement.services

import com.example.projectmanagement.models.mongo.SuperObject
import com.example.projectmanagement.repositories.mongo.SuperObjectRepository
import org.springframework.stereotype.Service

@Service
class SuperObjectService(
    private val repo: SuperObjectRepository
) {
    fun create(superObject: SuperObject): SuperObject = repo.save(superObject)

    fun getByFileId(fileId: Long): SuperObject? = repo.findByFileId(fileId)

    fun getById(id: String): SuperObject = repo.findById(id).orElseThrow { NoSuchElementException("Not found") }

    fun update(id: String, updated: SuperObject): SuperObject { updated.id = id; return repo.save(updated) }
    
    fun delete(id: String) = repo.deleteById(id)
}
