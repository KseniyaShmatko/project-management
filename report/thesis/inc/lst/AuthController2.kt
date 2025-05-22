    @PostMapping("/register")
    fun register(@RequestBody registerRequest: RegisterRequest): ResponseEntity<*> {
        if (userRepository.findByLogin(registerRequest.login)
        .isPresent) {
            return ResponseEntity.badRequest().body("Login is already taken!")
        }
        val user = User(
            name = registerRequest.name,
            surname = registerRequest.surname,
            login = registerRequest.login,
            passwordInternal = passwordEncoder.encode(registerRequest.password),
            photo = registerRequest.photo
        )
        val savedUser = userRepository.save(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(
            mapOf(
                "id" to savedUser.id,
                "name" to savedUser.name,
                "surname" to savedUser.surname,
                "login" to savedUser.login,
                "photo" to savedUser.photo
            )
        )
    }
    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal userDetails: User): ResponseEntity<Any> {
        return ResponseEntity.ok(mapOf(
            "id" to userDetails.id,
            "name" to userDetails.name,
            "surname" to userDetails.surname,
            "login" to userDetails.login,
            "photo" to userDetails.photo
        ))
    }
}
