package com.example.projectmanagement.services

import com.example.projectmanagement.controllers.dto.*
import com.example.projectmanagement.models.*
import com.example.projectmanagement.repositories.mongo.SuperObjectRepository // Если используется
import com.example.projectmanagement.repositories.* // Все репозитории
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.util.*
import org.hibernate.Hibernate

@Service
class ProjectFileService(
    private val projectFileRepository: ProjectFileRepository,
    private val projectRepository: ProjectRepository,
    private val fileRepository: FileRepository,
    private val fileTypeRepository: FileTypeRepository, // Добавить
    private val userRepository: UserRepository,         // Добавить
    private val projectService: ProjectService,         // Для checkAccessToProject
    private val superObjectRepository: SuperObjectRepository? = null // Сделал nullable на всякий
    // @Value("\${file.upload-dir}") private val uploadDir: String // Путь для загрузки файлов
) {
    private val uploadDir = "uploads" // Захардкодим пока, лучше из properties

    init {
        Files.createDirectories(Paths.get(uploadDir)) // Создаем директорию, если нет
    }

    private fun getCurrentUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || authentication.principal == "anonymousUser") {
            throw IllegalStateException("User not authenticated or authentication principal is not available.")
        }
        if (authentication.principal !is User) {
            throw IllegalStateException("Authentication principal is not of type User. Actual type: ${authentication.principal?.javaClass?.name}")
        }
        return authentication.principal as User
    }


    private fun File.toProjectFileResponseDto(): ProjectFileResponseDto {
        // Hibernate.initialize(this.type) - Уже есть в File.toResponseDto, но здесь мы преобразуем File, а не ProjectFile
        val fileTypeDto = this.type?.let { ft -> FileTypeResponseDto(id = ft.id, name = ft.name) }
            ?: throw IllegalStateException("FileType is null for file ${this.id}")

        return ProjectFileResponseDto(
            id = this.id,
            name = this.name,
            type = fileTypeDto,
            authorId = this.authorId,
            date = this.uploadDate.toString(),
            superObjectId = this.superObjectId
            // filePath здесь не нужен, если вы не отдаете его на фронт для прямого доступа,
            // а используете отдельный эндпоинт для скачивания
        )
    }


    @Transactional
    fun uploadAndLinkFile(projectId: Long, multipartFile: MultipartFile, typeId: Long): ProjectFileResponseDto {
        val currentUser = getCurrentUser()
        projectService.checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.EDITOR, ProjectRole.OWNER))

        val project = projectRepository.findById(projectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found") }
        val fileType = fileTypeRepository.findById(typeId)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type") }

        // Логика сохранения файла на диск
        val originalFilename = multipartFile.originalFilename ?: "file_${UUID.randomUUID()}"
        val filename = "${UUID.randomUUID()}_${originalFilename.replace("\\s+", "_")}"
        val targetLocation = Paths.get(uploadDir).resolve(filename)
        Files.copy(multipartFile.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)

        val fileEntity = File(
            name = originalFilename, // или имя, переданное клиентом
            type = fileType,
            authorId = currentUser.id,
            uploadDate = LocalDateTime.now(),
            filePath = targetLocation.toString() // Сохраняем относительный или абсолютный путь
        )
        val savedFile = fileRepository.save(fileEntity)

        val projectFile = ProjectFile(project = project, file = savedFile)
        projectFileRepository.save(projectFile)

        return savedFile.toProjectFileResponseDto()
    }

    @Transactional
    fun linkExistingFile(projectId: Long, fileId: Long): ProjectFile {
        val currentUser = getCurrentUser()
        projectService.checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.EDITOR, ProjectRole.OWNER))

        val project = projectRepository.findById(projectId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found") }
        val file = fileRepository.findById(fileId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "File to link not found") }

        // Доп. проверка: может ли currentUser "делиться" этим файлом? (например, он автор)
        if (file.authorId != currentUser.id) {
             // Или если файл публичный, или другая логика
            // throw ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the author of this file to link it.")
        }
        
        if (projectFileRepository.existsByProject_IdAndFile_Id(projectId, fileId)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "File is already linked to this project.")
        }

        val projectFile = ProjectFile(project = project, file = file)
        return projectFileRepository.save(projectFile)
    }
    
    @Transactional(readOnly = true)
    fun getFilesForProjectWithDetails(projectId: Long): List<ProjectFileResponseDto> {
        val currentUser = getCurrentUser()
        projectService.checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.VIEWER, ProjectRole.EDITOR, ProjectRole.OWNER))

        val projectFiles = projectFileRepository.findAllByProject_Id(projectId)
        return projectFiles.map { pf ->
            // pf.file должен быть загружен (по умолчанию EAGER для @ManyToOne, если LAZY - нужна инициализация)
            Hibernate.initialize(pf.file) // если LAZY
            Hibernate.initialize(pf.file.type) // если LAZY
            pf.file.toProjectFileResponseDto()
        }
    }

    @Transactional(readOnly = true)
    fun getFileDetailsInProject(projectId: Long, fileId: Long): ProjectFileResponseDto {
        val currentUser = getCurrentUser()
        projectService.checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.VIEWER, ProjectRole.EDITOR, ProjectRole.OWNER))

        val projectFile = projectFileRepository.findByProject_IdAndFile_Id(projectId, fileId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in this project")
        
        Hibernate.initialize(projectFile.file)
        Hibernate.initialize(projectFile.file.type)
        return projectFile.file.toProjectFileResponseDto()
    }

    @Transactional
    fun updateFileNameInProject(projectId: Long, fileId: Long, newName: String): ProjectFileResponseDto {
        val currentUser = getCurrentUser()
        projectService.checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.EDITOR, ProjectRole.OWNER))

        val file = projectFileRepository.findByProject_IdAndFile_Id(projectId, fileId)?.file
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in this project")
        
        file.name = newName
        val savedFile = fileRepository.save(file)
        Hibernate.initialize(savedFile.type)
        return savedFile.toProjectFileResponseDto()
    }
    
    @Transactional
    fun updateSuperObjectIdInProject(projectId: Long, fileId: Long, superObjectId: String?): ProjectFileResponseDto {
        val currentUser = getCurrentUser()
        projectService.checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.EDITOR, ProjectRole.OWNER))

        val file = projectFileRepository.findByProject_IdAndFile_Id(projectId, fileId)?.file
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in this project")
        
        file.superObjectId = superObjectId
        val savedFile = fileRepository.save(file)
        Hibernate.initialize(savedFile.type)
        return savedFile.toProjectFileResponseDto()
    }

    @Transactional
    fun unlinkFile(projectId: Long, fileId: Long) {
        val currentUser = getCurrentUser()
        projectService.checkAccessToProject(projectId, currentUser.id, listOf(ProjectRole.EDITOR, ProjectRole.OWNER))

        val projectFile = projectFileRepository.findByProject_IdAndFile_Id(projectId, fileId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "File is not linked to this project.")
        
        projectFileRepository.delete(projectFile)

        // Опционально: если файл больше нигде не используется и был создан только для этого проекта, удалить его
        // val fileUsageCount = projectFileRepository.countByFile_Id(fileId)
        // if (fileUsageCount == 0) {
        //     // Удалить файл с диска
        //     // fileRepository.deleteById(fileId)
        // }
    }


    // Старые методы, которые были в вашем ProjectFileService, возможно, нужно будет адаптировать или удалить,
    // если новый контроллер покрывает их функциональность с проверками прав.
    // fun getFilesForProject(projectId: Long): List<File> - вероятно, заменяется на getFilesForProjectWithDetails
}
