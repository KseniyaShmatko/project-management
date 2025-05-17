package com.example.projectmanagement.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Files
import java.nio.file.Paths

@Configuration
class WebConfig : WebMvcConfigurer {

    @Value("\${file.upload-dir:\${user.home}/uploads_data}")
    private lateinit var uploadDir: String

    private val resourceHttpPath = "/uploads/**" 

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "http://localhost:3000",
            )
            .allowedMethods(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
            )
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val uploadPath = Paths.get(uploadDir)

        if (Files.notExists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath)
                println("Created upload directory: ${uploadPath.toAbsolutePath()}")
            } catch (e: Exception) {
                println("Could not create upload directory: ${uploadPath.toAbsolutePath()}. Error: ${e.message}")
            }
        } else {
            println("Upload directory already exists: ${uploadPath.toAbsolutePath()}")
        }

        if (Files.exists(uploadPath) && Files.isDirectory(uploadPath)) {
            registry.addResourceHandler(resourceHttpPath)
                .addResourceLocations("file:${uploadPath.toAbsolutePath().normalize()}/")
            println("Serving static files from $resourceHttpPath mapped to directory file:${uploadPath.toAbsolutePath().normalize()}/")
        } else {
            println("WARNING: Upload directory '$uploadDir' does not exist or is not a directory. Static files will not be served from it.")
        }
    }
}
