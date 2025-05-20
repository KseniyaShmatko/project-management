package com.example.projectmanagement.controllers

import com.example.projectmanagement.controllers.dto.ProjectUserDto
import com.example.projectmanagement.controllers.dto.ProjectUserView
import com.example.projectmanagement.controllers.dto.toView // Импорт расширения
import com.example.projectmanagement.models.ProjectRole
import com.example.projectmanagement.services.ProjectUserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/projects-users") // Прежнее имя контроллера было ProjectUserController, но маппинг хороший
class ProjectAccessController(private val service: ProjectUserService) { // Переименовал для ясности

    @PostMapping
    fun linkUserToProject(@RequestBody dto: ProjectUserDto): ResponseEntity<ProjectUserView> {
        val pu = service.linkUserToProject(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(pu.toView())
    }

    // Этот эндпоинт может быть не нужен, если ProjectService.getProjectsForCurrentUser уже учитывает ProjectUser
    // Или он полезен, если нужно получить ТОЛЬКО проекты, к которым пользователь добавлен как участник (не владелец)
    // @GetMapping("/user/{user_id}")
    // fun getProjectAccessForUser(@PathVariable user_id: Long): List<ProjectUserView> {
    //     return service.getProjectsForUser(user_id).map { it.toView() }
    // }

    @GetMapping("/project/{project_id}/users") // Более RESTful путь
    fun getUsersForProject(@PathVariable project_id: Long): ResponseEntity<List<ProjectUserView>> {
        return ResponseEntity.ok(service.getUsersForProject(project_id).map { it.toView() })
    }

    // DTO для обновления роли
    data class UpdateUserRoleDto(val role: ProjectRole)

    @PutMapping("/project/{project_id}/user/{user_id}") // Более RESTful путь
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
