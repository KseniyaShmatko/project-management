package com.example.projectmanagement.repositories.mongo

import com.example.projectmanagement.models.mongo.ContentBlock
import org.springframework.data.mongodb.repository.MongoRepository

interface ContentBlockRepository : MongoRepository<ContentBlock, String> {
    fun findByObjectTypeAndIdIn(objectType: String, ids: List<String>): List<ContentBlock>
}
