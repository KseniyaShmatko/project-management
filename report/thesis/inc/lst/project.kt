@ExtendWith(MockitoExtension::class)
class ProjectServiceTest {
    @Mock
    lateinit var projectRepository: ProjectRepository
    @Mock
    lateinit var userRepository: UserRepository
    @Mock
    lateinit var projectUserService: ProjectUserService
    @InjectMocks
    lateinit var projectService: ProjectService
    @Test
    fun `test create project success`() {
        val projectName = "My New Project"
        val ownerLogin = "ownerUser"
        val ownerId = 1L
        val ownerUser = User(ownerId, "Owner", "Test", ownerLogin, "pass", null)
        val projectToSave = Project(name = projectName, owner = ownerUser)
        val savedProject = Project(id=100L, name = projectName, owner = ownerUser) 
        val authentication = mock(Authentication::class.java)
        val securityContext = mock(SecurityContext::class.java)
        `when`(securityContext.authentication)
        .thenReturn(authentication)
        SecurityContextHolder.setContext(securityContext)
        `when`(authentication.principal).thenReturn(ownerUser)
        `when`(userRepository.findById(ownerId))
        .thenReturn(Optional.of(ownerUser))
        `when`(projectRepository.save(any(Project::class.java)))
        .thenReturn(savedProject)
        val createdProjectDto = projectService.createProject(projectToSave)
        assertNotNull(createdProjectDto)
        assertEquals(projectName, createdProjectDto.name)
        assertEquals(ownerId, createdProjectDto.owner.id)
        verify(projectRepository).save(any(Project::class.java))
    }
}