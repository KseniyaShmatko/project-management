@SpringBootTest
@AutoConfigureMockMvc
class CreateNoteE2ETest {
    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `should create a note after auth and project setup`() {
        val registerRequest = RegisterRequest(
            name = "John",
            surname = "Doe",
            login = "johndoe",
            password = "password123"
        )

        mockMvc.perform(post("/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper
            .writeValueAsString(registerRequest)))
            .andExpect(status().isCreated)

        val loginRequest = LoginRequest(
            login = "johndoe",
            password = "password123"
        )

        val loginResult = mockMvc.perform(post("/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper
            .writeValueAsString(loginRequest)))
            .andExpect(status().isOk)
            .andReturn()

        val jwtResponse = objectMapper.readValue(
            loginResult.response.contentAsString,
            JwtResponse::class.java
        )
        val token = jwtResponse.token!!
