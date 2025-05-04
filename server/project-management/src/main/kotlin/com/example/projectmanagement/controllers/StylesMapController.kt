package com.example.projectmanagement.controllers

import com.example.projectmanagement.models.mongo.StylesMap
import com.example.projectmanagement.services.StylesMapService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/styles-maps")
class StylesMapController(private val service: StylesMapService) {
    @PostMapping fun create(@RequestBody s: StylesMap) = service.create(s)

    @GetMapping("/{id}") fun getById(@PathVariable id: String) = service.getById(id)

    @PutMapping("/{id}") fun update(@PathVariable id: String, @RequestBody s: StylesMap) = service.update(id, s)
    
    @DeleteMapping("/{id}") fun delete(@PathVariable id: String) = service.delete(id)
}
