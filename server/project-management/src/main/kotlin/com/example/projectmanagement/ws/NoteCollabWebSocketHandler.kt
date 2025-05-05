package com.example.projectmanagement.ws

import com.example.projectmanagement.services.ContentBlockService
import com.example.projectmanagement.models.mongo.ContentBlock
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.TextWebSocketHandler

data class BlockEvent(
    val noteId: String,
    val userId: Long?,
    val type: String,             // "block-create" | "block-edit" | "block-delete"
    val block: ContentBlock? = null,
    val blockId: String? = null
)

@Component
class NoteCollabWebSocketHandler(
    private val contentBlockService: ContentBlockService
) : TextWebSocketHandler() {
    private val registry = NoteSessionRegistry()
    private val mapper = jacksonObjectMapper()

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val event = mapper.readValue<BlockEvent>(message.payload)
            val noteId = event.noteId

            registry.addSession(noteId, session)

            when (event.type) {
                "block-create" -> {
                    event.block?.let { contentBlockService.create(it) }
                }
                "block-edit" -> {
                    event.block?.let { contentBlockService.update(it.id ?: error("No id"), it) }
                }
                "block-delete" -> {
                    event.blockId?.let { contentBlockService.delete(it) }
                }
            }

            registry.broadcast(noteId, message.payload, exclude = session)
        } catch (e: Exception) {
            session.sendMessage(TextMessage("{\"error\":\"bad event format\"}"))
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        registry.removeSession(session)
    }
}
