        val fileDto = FileDto(
            name = "Test Note",
            typeId = fileTypeId,
            authorId = userId
        )

        val fileResult = mockMvc.perform(post("/files")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(fileDto)))
            .andExpect(status().isOk)
            .andReturn()

        val fileId = objectMapper.readTree(fileResult.response
        .contentAsString).get("id").asLong()

        mockMvc.perform(post("/projects/$projectId/files/link")
            .header("Authorization", "Bearer $token")
            .param("file_id", fileId.toString()))
            .andExpect(status().isOk)

        mockMvc.perform(get("/projects/$projectId/files")
            .header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("Test Note"))
    }
}
