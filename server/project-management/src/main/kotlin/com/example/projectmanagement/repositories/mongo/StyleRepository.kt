package com.example.projectmanagement.repositories.mongo

import com.example.projectmanagement.models.mongo.Style
import org.springframework.data.mongodb.repository.MongoRepository

interface StyleRepository : MongoRepository<Style, String>