package com.example.projectmanagement.controllers

import com.example.projectmanagement.models.User
import com.example.projectmanagement.services.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {

    @PostMapping("/register")
    fun registerUser(@RequestBody user: User): User {
        return userService.registerUser(user)
    }

    @GetMapping("/{user_id}")
    fun getUser(@PathVariable user_id: Long): User {
        return userService.getUserById(user_id)
    }

    @PutMapping("/{user_id}")
    fun updateUser(@PathVariable user_id: Long, @RequestBody updateUser: User): User {
        return userService.updateUser(user_id, updateUser)
    }

    @DeleteMapping("/{user_id}")
    fun deleteUser(@PathVariable user_id: Long) {
        userService.deleteUser(user_id)
    }
}
