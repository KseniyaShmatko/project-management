package com.example.projectmanagement.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class JacksonConfig {
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        
        // Добавляем поддержку Kotlin data classes
        mapper.registerModule(KotlinModule.Builder().build())
        
        mapper.registerModule(JavaTimeModule())
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        
        return mapper
    }
}
