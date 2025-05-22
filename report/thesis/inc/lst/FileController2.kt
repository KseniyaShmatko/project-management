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