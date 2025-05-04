package com.example.projectmanagement.controllers

import com.example.projectmanagement.models.File
import com.example.projectmanagement.models.FileType
import com.example.projectmanagement.services.FileService
import com.example.projectmanagement.services.FileTypeService
import com.example.projectmanagement.controllers.dto.FileDto
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/files")
class FileController(private val fileService: FileService) {

    @PostMapping
    fun createFile(@RequestBody dto: FileDto): File =
        fileService.createFile(dto)

    @GetMapping("/{file_id}")
    fun getFile(@PathVariable file_id: Long): File =
        fileService.getFileById(file_id)

    @PutMapping("/{file_id}")
    fun updateFile(@PathVariable file_id: Long, @RequestBody updateDto: FileDto): File =
        fileService.updateFile(file_id, updateDto)

    @DeleteMapping("/{file_id}")
    fun deleteFile(@PathVariable file_id: Long) =
        fileService.deleteFile(file_id)
}

@RestController
@RequestMapping("/file-types")
class FileTypeController(private val service: FileTypeService) {

    @PostMapping
    fun createType(@RequestBody req: Map<String, String>) =
        service.createType(req["name"] ?: throw IllegalArgumentException("name required"))

    @GetMapping
    fun getAll() = service.getAllTypes()
}