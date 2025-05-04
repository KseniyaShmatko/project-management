package com.example.projectmanagement.controllers

import com.example.projectmanagement.controllers.dto.ProjectUserDto
import com.example.projectmanagement.controllers.dto.ProjectUserView
import com.example.projectmanagement.models.ProjectUser
import com.example.projectmanagement.services.ProjectUserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/projects-users")
class ProjectUserController(private val service: ProjectUserService) {

    @PostMapping
    fun linkUserToProject(@RequestBody dto: ProjectUserDto): ProjectUserView {
        val pu = service.linkUserToProject(dto)
        return ProjectUserView(pu.id, pu.project.id, pu.user.id, pu.role, pu.permission)
    }

    @GetMapping("/user/{user_id}")
    fun getProjectsForUser(@PathVariable user_id: Long): List<ProjectUserView> {
        return service.getProjectsForUser(user_id).map {
            ProjectUserView(
                it.id,
                it.project.id,
                it.user.id,
                it.role,
                it.permission
            )
        }
    }

    @GetMapping("/project/{project_id}")
    fun getUsersForProject(@PathVariable project_id: Long): List<ProjectUserView> {
        return service.getUsersForProject(project_id).map {
            ProjectUserView(
                it.id,
                it.project.id,
                it.user.id,
                it.role,
                it.permission
            )
        }
    }

    @PutMapping
    fun updateUserProjectLink(@RequestBody dto: ProjectUserDto): ProjectUserView {
        val pu = service.updateRoleAndPermission(dto.projectId, dto.userId, dto.role, dto.permission)
        return ProjectUserView(pu.id, pu.project.id, pu.user.id, pu.role, pu.permission)
    }
}