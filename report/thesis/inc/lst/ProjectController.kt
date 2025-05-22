@RestController
@RequestMapping("/projects")
class ProjectController(private val projectService: ProjectService) {
    @PostMapping
    fun createProject(@RequestBody project: Project): ResponseEntity<ProjectResponseDto> {
        return ResponseEntity.status(HttpStatus.CREATED)
        .body(projectService.createProject(project))
    }
    @GetMapping
    fun getCurrentUserProjects(): ResponseEntity<List<ProjectResponseDto>> {
         return ResponseEntity.ok(projectService
         .getProjectsForCurrentUser())
    }
    @GetMapping("/{project_id}")
    fun getProject(@PathVariable project_id: Long): ResponseEntity<ProjectResponseDto> {
        return ResponseEntity.ok(projectService
        .getProjectById(project_id))
    }
    @PutMapping("/{project_id}")
    fun updateProject(@PathVariable project_id: Long, @RequestBody update: Project): ResponseEntity<ProjectResponseDto> {
        return ResponseEntity.ok(projectService
        .updateProject(project_id, update))
    }
    @DeleteMapping("/{project_id}")
    fun deleteProject(@PathVariable project_id: Long): ResponseEntity<Void> {
        projectService.deleteProject(project_id)
        return ResponseEntity.noContent().build()
    }
}