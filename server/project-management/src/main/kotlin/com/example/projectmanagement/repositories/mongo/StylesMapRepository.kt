package com.example.projectmanagement.repositories.mongo

import com.example.projectmanagement.models.mongo.StylesMap
import org.springframework.data.mongodb.repository.MongoRepository

interface StylesMapRepository : MongoRepository<StylesMap, String>