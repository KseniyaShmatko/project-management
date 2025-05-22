@RestController
@RequestMapping("/files-storage")
class FileStorageController(private val fileStorageService: FileStorageService) {
    @PostMapping("/upload/image") 
    fun uploadImage(@RequestParam("image") file: MultipartFile): ResponseEntity<FileUploadResponse> {
        try {
            val response = fileStorageService.storeFile(file)
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            val errorResponse = FileUploadResponse(
                success = 0, 
                file = com.example.projectmanagement.controllers
                .dto.FileDetails(
                    url = "", 
                    name = file.originalFilename)
            )
            println("Error uploading file: ${e.message}")
            return ResponseEntity.status(HttpStatus
            .INTERNAL_SERVER_ERROR).body(errorResponse)
        }
    }
}
