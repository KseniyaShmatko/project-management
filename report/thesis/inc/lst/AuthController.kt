@RestController
@RequestMapping("/users")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @PostMapping("/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<*> {
        return try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.login, loginRequest.password)
            )
            SecurityContextHolder.getContext().authentication = authentication
            val user = authentication.principal as User
            val token = jwtTokenProvider.createToken(user.login, user.id)
            ResponseEntity.ok(JwtResponse(
                token = token,
                id = user.id,
                login = user.login,
                name = user.name,
                surname = user.surname,
                photo = user.photo
            ))
        } catch (e: AuthenticationException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body("Invalid login or password")
        }
    }