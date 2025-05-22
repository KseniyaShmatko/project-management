@RestController
@RequestMapping("/projects/{project_id}/files/{file_id}/download")
class FileDownloadController(private val fileDownloadService: FileDownloadService) {

    @GetMapping
    fun downloadFile(
        @PathVariable project_id: Long,
        @PathVariable file_id: Long
    ): ResponseEntity<Resource> {
        val resource = fileDownloadService.getFileAsResource(project_id, file_id)
        val filename = resource.filename ?: "downloaded_file"

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .body(resource)
    }
}
