package com.example.projectmanagement.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "files")
data class File(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var name: String,

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "type_id", nullable = false)
    var type: FileType?, // Если тип всегда есть ПЕРЕД сохранением, то FileType (без ?)

    @Column(name = "author") // Явно указываем, что это ID автора
    val authorId: Long,

    @Column(name = "upload_date")
    var uploadDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "super_object_id")
    var superObjectId: String? = null,
    
    @Column(name = "file_path") // Путь к файлу на диске/в хранилище
    var filePath: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as File
        return if (id == 0L) false else id == other.id
    }

    override fun hashCode(): Int {
        return if (id != 0L) id.hashCode() else System.identityHashCode(this)
    }

    override fun toString(): String {
        return "File(id=$id, name='$name', typeId=${type?.id}, authorId=$authorId)"
    }
}

@Entity
@Table(name = "file_types")
data class FileType(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as FileType
        return if (id == 0L) false else id == other.id
    }

    override fun hashCode(): Int {
        return if (id != 0L) id.hashCode() else System.identityHashCode(this)
    }

    override fun toString(): String {
        return "FileType(id=$id, name='$name')"
    }
}
