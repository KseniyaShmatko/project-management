@RestController
@RequestMapping("/projects-users")
class ProjectAccessController(private val service: ProjectUserService) {
    @PostMapping
    fun linkUserToProject(@RequestBody dto: ProjectUserDto): ResponseEntity<ProjectUserView> {
        val pu = service.linkUserToProject(dto)
        return ResponseEntity.status(HttpStatus.CREATED)
        .body(pu.toView())
    }
    @GetMapping("/project/{project_id}/users")
    fun getUsersForProject(@PathVariable project_id: Long): ResponseEntity<List<ProjectUserView>> {
        return ResponseEntity.ok(service
        .getUsersForProject(project_id).map { it.toView() })
    }
    data class UpdateUserRoleDto(val role: ProjectRole)
    @PutMapping("/project/{project_id}/user/{user_id}")
    fun updateUserProjectRole(
        @PathVariable project_id: Long,
        @PathVariable user_id: Long,
        @RequestBody updateDto: UpdateUserRoleDto
    ): ResponseEntity<ProjectUserView> {
        val pu = service.updateUserProjectRole(project_id, user_id, updateDto.role)
        return ResponseEntity.ok(pu.toView())
    }
    @DeleteMapping("/project/{project_id}/user/{user_id}")
    fun removeUserFromProject(
        @PathVariable project_id: Long,
        @PathVariable user_id: Long
    ): ResponseEntity<Void> {
        service.removeUserFromProject(project_id, user_id)
        return ResponseEntity.noContent().build()
    }
}
