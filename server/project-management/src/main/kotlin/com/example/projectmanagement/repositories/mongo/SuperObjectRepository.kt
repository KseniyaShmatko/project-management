package com.example.projectmanagement.repositories.mongo

import com.example.projectmanagement.models.mongo.SuperObject
import org.springframework.data.mongodb.repository.MongoRepository

interface SuperObjectRepository : MongoRepository<SuperObject, String> {
    fun findByFileId(fileId: Long): SuperObject?
}
