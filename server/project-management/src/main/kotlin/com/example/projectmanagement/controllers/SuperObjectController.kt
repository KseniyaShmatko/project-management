package com.example.projectmanagement.controllers

import com.example.projectmanagement.models.mongo.SuperObject
import com.example.projectmanagement.services.SuperObjectService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/super-objects")
class SuperObjectController(private val service: SuperObjectService) {
    @PostMapping fun create(@RequestBody s: SuperObject) = service.create(s)

    @GetMapping("/by-file/{fileId}") fun getByFileId(@PathVariable fileId: Long) = service.getByFileId(fileId)

    @GetMapping("/{id}") fun getById(@PathVariable id: String) = service.getById(id)

    @PutMapping("/{id}") fun update(@PathVariable id: String, @RequestBody s: SuperObject) = service.update(id, s)
    
    @DeleteMapping("/{id}") fun delete(@PathVariable id: String) = service.delete(id)
}
