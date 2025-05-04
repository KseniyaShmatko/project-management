package com.example.projectmanagement.controllers

import com.example.projectmanagement.models.Project
import com.example.projectmanagement.services.ProjectService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/projects")
class ProjectController(private val projectService: ProjectService) {

    @PostMapping
    fun createProject(@RequestBody project: Project): Project =
        projectService.createProject(project)

    @GetMapping("/{project_id}")
    fun getProject(@PathVariable project_id: Long): Project =
        projectService.getProjectById(project_id)

    @PutMapping("/{project_id}")
    fun updateProject(@PathVariable project_id: Long, @RequestBody update: Project): Project =
        projectService.updateProject(project_id, update)

    @DeleteMapping("/{project_id}")
    fun deleteProject(@PathVariable project_id: Long) =
        projectService.deleteProject(project_id)
}
