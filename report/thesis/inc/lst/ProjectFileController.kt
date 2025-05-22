@RestController
@RequestMapping("/projects/{project_id}/files")
class ProjectFileController(private val projectFileService: ProjectFileService) {
    @PostMapping("/upload")
    fun uploadAndLinkFileToProject(
        @PathVariable("project_id") projectId: Long,
        @RequestParam("file") multipartFile: MultipartFile,
        @RequestParam("type_id") typeId: Long
    ): ResponseEntity<ProjectFileResponseDto> {
        val createdFileDto = projectFileService.uploadAndLinkFile(projectId, multipartFile, typeId)
        return ResponseEntity.status(HttpStatus.CREATED)
        .body(createdFileDto)
    }

    @PostMapping("/link")
    fun linkExistingFileToProject(
        @PathVariable("project_id") projectId: Long,
        @RequestParam("file_id") fileId: Long
    ): ResponseEntity<Map<String, Long>> {
        val projectFile = projectFileService.linkExistingFile(projectId, fileId)
        return ResponseEntity.ok(mapOf("project_id" to projectFile.project.id, "file_id" to projectFile.file.id))
    }

    @GetMapping
    fun getFilesForProject(@PathVariable("project_id") projectId: Long): ResponseEntity<List<ProjectFileResponseDto>> {
        return ResponseEntity.ok(projectFileService
        .getFilesForProjectWithDetails(projectId))
    }