@RestController
@RequestMapping("/content-blocks")
class ContentBlockController(private val service: ContentBlockService) {
    @PostMapping fun create(@RequestBody s: ContentBlock) = service.create(s)

    @GetMapping("/{id}") fun getById(@PathVariable id: String) = service.getById(id)

    @PutMapping("/{id}") fun update(@PathVariable id: String, @RequestBody s: ContentBlock) = service.update(id, s)
    
    @DeleteMapping("/{id}") fun delete(@PathVariable id: String) = service.delete(id)
}
