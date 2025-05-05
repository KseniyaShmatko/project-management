package com.example.projectmanagement.ws

import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

class NoteSessionRegistry {
    private val sessions: MutableMap<String, MutableSet<WebSocketSession>> = ConcurrentHashMap()

    fun addSession(noteId: String, session: WebSocketSession) {
        sessions.computeIfAbsent(noteId) { mutableSetOf() }.add(session)
    }
    fun removeSession(session: WebSocketSession) {
        sessions.values.forEach { it.remove(session) }
    }
    fun broadcast(noteId: String, message: String, exclude: WebSocketSession? = null) {
        sessions[noteId]?.filter { it.isOpen && it != exclude }?.forEach {
            it.sendMessage(org.springframework.web.socket.TextMessage(message))
        }
    }
}
