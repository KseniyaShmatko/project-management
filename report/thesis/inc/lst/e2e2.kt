        val userId = jwtResponse.id!!

        val fileTypeResult = mockMvc.perform(post("/file-types")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name": "note"}"""))
            .andExpect(status().isOk)
            .andReturn()

        val fileTypeId = objectMapper.readTree(fileTypeResult.response
        .contentAsString).get("id").asLong()

        val project = Project(name = "Test Project", description = "Project for testing")
        val projectResult = mockMvc.perform(post("/projects")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(project)))
            .andExpect(status().isCreated)
            .andReturn()

        val projectId = objectMapper.readTree(projectResult.response
        .contentAsString).get("id").asLong()

        val projectUserDto = ProjectUserDto(
            projectId = projectId,
            userId = userId,
            role = ProjectRole.OWNER
        )

        mockMvc.perform(post("/projects-users")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper
            .writeValueAsString(projectUserDto)))
            .andExpect(status().isCreated)