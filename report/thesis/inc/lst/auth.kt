@ExtendWith(MockitoExtension::class)
class AuthControllerTest {
    @Mock
    lateinit var userRepository: UserRepository
    @Mock
    lateinit var passwordEncoder: PasswordEncoder
    @Mock
    lateinit var authenticationManager: AuthenticationManager 
    @Mock
    lateinit var jwtTokenProvider: JwtTokenProvider

    @InjectMocks
    lateinit var authController: AuthController

    @Test
    fun `test register user success`() {
        val request = RegisterRequest("Test", "User", "testlogin", "password123", null)
        val encodedPassword = "encodedPassword"
        val savedUser = User(1L, "Test", "User", "testlogin", encodedPassword, null)

        `when`(userRepository.findByLogin(request.login))
        .thenReturn(Optional.empty())
        `when`(passwordEncoder.encode(request.password))
        .thenReturn(encodedPassword)
        `when`(userRepository.save(any(User::class.java)))
        .thenReturn(savedUser)

        val response = authController.register(request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        verify(userRepository).findByLogin(request.login)
        verify(passwordEncoder).encode(request.password)
        verify(userRepository).save(any(User::class.java))
    }
}