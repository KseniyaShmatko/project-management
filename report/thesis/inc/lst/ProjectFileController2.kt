    @GetMapping("/{file_id}")
    fun getFileInProject(
        @PathVariable("project_id") projectId: Long,
        @PathVariable("file_id") fileId: Long
    ): ResponseEntity<ProjectFileResponseDto> {
        val fileDto = projectFileService.getFileDetailsInProject(projectId, fileId)
        return ResponseEntity.ok(fileDto)
    }
    @PatchMapping("/{file_id}/name")
    fun updateFileNameInProject(
        @PathVariable("project_id") projectId: Long,
        @PathVariable("file_id") fileId: Long,
        @RequestBody dto: FileUpdateNameDto
    ): ResponseEntity<ProjectFileResponseDto> {
        val updatedFileDto = projectFileService.updateFileNameInProject(projectId, fileId, dto.name)
        return ResponseEntity.ok(updatedFileDto)
    }
    @PatchMapping("/{file_id}/super-object")
    fun updateSuperObjectIdInProject(
        @PathVariable("project_id") projectId: Long,
        @PathVariable("file_id") fileId: Long,
        @RequestBody request: Map<String, String?>
    ): ResponseEntity<ProjectFileResponseDto> {
        val superObjectId = request["superObjectId"]
        val updatedFileDto = projectFileService.updateSuperObjectIdInProject(projectId, fileId, superObjectId)
        return ResponseEntity.ok(updatedFileDto)
    }
    @DeleteMapping("/{file_id}")
    fun unlinkFileFromProject(
        @PathVariable("project_id") projectId: Long,
        @PathVariable("file_id") fileId: Long
    ): ResponseEntity<Void> {
        projectFileService.unlinkFile(projectId, fileId)
        return ResponseEntity.noContent().build()
    }
}
