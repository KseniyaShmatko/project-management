package com.example.projectmanagement.controllers

import com.example.projectmanagement.models.File
import com.example.projectmanagement.models.FileType
import com.example.projectmanagement.services.FileService
import com.example.projectmanagement.services.FileTypeService
import com.example.projectmanagement.controllers.dto.FileDto
import com.example.projectmanagement.controllers.dto.FileResponseDto
import com.example.projectmanagement.controllers.dto.toResponseDto
import com.example.projectmanagement.controllers.dto.FileUpdateNameDto
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/files")
class FileController(private val fileService: FileService) {

    @PostMapping
    fun createFile(@RequestBody dto: FileDto): FileResponseDto =
        fileService.createFile(dto).toResponseDto()

    @GetMapping("/{file_id}")
    fun getFile(@PathVariable file_id: Long): FileResponseDto =
        fileService.getFileById(file_id).toResponseDto()

    @PutMapping("/{file_id}")
    fun updateFile(@PathVariable file_id: Long, @RequestBody updateDto: FileDto): FileResponseDto =
        fileService.updateFile(file_id, updateDto).toResponseDto()

    @PatchMapping("/{file_id}/super-object")
    fun updateSuperObjectId(
        @PathVariable file_id: Long,
        @RequestBody request: Map<String, String?>
    ): FileResponseDto {
        val superObjectId = request["superObjectId"]
        return fileService.updateSuperObjectId(file_id, superObjectId).toResponseDto()
    }

    @PatchMapping("/{file_id}/name")
    fun updateFileName(
        @PathVariable file_id: Long,
        @RequestBody dto: FileUpdateNameDto
    ): FileResponseDto {
        return fileService.updateFileName(file_id, dto.name).toResponseDto()
    }

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