@RestController
@RequestMapping("/styles")
class StyleController(private val service: StyleService) {
    @PostMapping fun create(@RequestBody s: Style) = service.create(s)

    @GetMapping("/{id}") fun getById(@PathVariable id: String) = service.getById(id)

    @PutMapping("/{id}") fun update(@PathVariable id: String, @RequestBody s: Style) = service.update(id, s)
    
    @DeleteMapping("/{id}") fun delete(@PathVariable id: String) = service.delete(id)
}
