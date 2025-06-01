package com.example.projectmanagement.services

import com.example.projectmanagement.controllers.dto.FileUploadResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.mock.web.MockMultipartFile
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileStorageServiceTest {

    private lateinit var fileStorageService: FileStorageService
    private lateinit var uploadDir: String

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setup() {
        uploadDir = tempDir.toString()
        fileStorageService = FileStorageService(uploadDir)
    }

    @Test
    fun `storeFile - when file has invalid path - should return error response`() {
        val fileName = "../invalid/path.txt"
        val file = MockMultipartFile(
            "file",
            fileName,
            "text/plain",
            "test".toByteArray()
        )

        val result = fileStorageService.storeFile(file)

        assertEquals(0, result.success)
        assertEquals(fileName, result.file.name)
        assertEquals("", result.file.url)
    }
}
