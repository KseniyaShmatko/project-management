@RestController
@RequestMapping("/super-objects")
class SuperObjectController(private val service: SuperObjectService) {
    @PostMapping fun create(@RequestBody s: SuperObject) = service.create(s)

    @GetMapping("/by-file/{fileId}") fun getByFileId(@PathVariable fileId: Long) = service.getByFileId(fileId)

    @GetMapping("/{id}") fun getById(@PathVariable id: String) = service.getById(id)

    @PutMapping("/{id}") fun update(@PathVariable id: String, @RequestBody s: SuperObject) = service.update(id, s)
    
    @DeleteMapping("/{id}") fun delete(@PathVariable id: String) = service.delete(id)

    @PutMapping("/{superObjectId}/sync-blocks")
    fun syncDocumentBlocks(
        @PathVariable superObjectId: String,
        @RequestBody blocksPayload: List<EditorJsBlockDto>
    ): ResponseEntity<SuperObject> {
        val updatedSuperObject = service.syncDocumentBlocks(superObjectId, blocksPayload)
        return ResponseEntity.ok(updatedSuperObject)
    }
}
