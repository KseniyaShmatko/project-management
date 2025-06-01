package com.example.projectmanagement.models.mongo

import com.example.projectmanagement.models.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ModelTest {

    @Test
    fun `test CalendarEvent full API`() {
        val now = Date()
        val event1 = CalendarEvent(
            id = "event1",
            superObjectId = "obj1",
            title = "Meeting",
            description = "Project kickoff",
            startTime = now,
            endTime = now,
            participants = listOf(1, 2, 3)
        )

        val event2 = event1.copy()
        assertEquals(event1, event2)
        assertEquals(event1.hashCode(), event2.hashCode())
        assertTrue(event1.toString().contains("Meeting"))

        event2.title = "Updated"
        assertEquals("Updated", event2.title)
    }

    @Test
    fun `test SchemeEdge full API`() {
        val edge1 = SchemeEdge(
            id = "edge1",
            superObjectId = "obj1",
            type = "directed",
            sourceNodeId = "nodeA",
            targetNodeId = "nodeB",
            data = mapOf("weight" to 5)
        )

        val edge2 = edge1.copy()
        assertEquals(edge1, edge2)
        assertEquals(edge1.hashCode(), edge2.hashCode())
        assertTrue(edge1.toString().contains("directed"))

        edge2.type = "undirected"
        assertEquals("undirected", edge2.type)
    }

    @Test
    fun `test SchemeNode full API`() {
        val node1 = SchemeNode(
            id = "node1",
            superObjectId = "obj1",
            type = "task",
            x = 10,
            y = 20,
            width = 100,
            height = 200,
            data = mapOf("color" to "blue")
        )

        val node2 = node1.copy()
        assertEquals(node1, node2)
        assertEquals(node1.hashCode(), node2.hashCode())
        assertTrue(node1.toString().contains("blue"))

        node2.height = 300
        assertEquals(300, node2.height)
    }

    @Test
    fun `test TaskColumn full API`() {
        val column1 = TaskColumn(
            id = "col1",
            superObjectId = "obj1",
            name = "To Do",
            order = 1
        )

        val column2 = column1.copy()
        assertEquals(column1, column2)
        assertEquals(column1.hashCode(), column2.hashCode())
        assertTrue(column1.toString().contains("To Do"))

        column2.name = "Done"
        assertEquals("Done", column2.name)
    }

    @Test
    fun `test TaskItem getters and setters`() {
        val now = Date()
        val task = TaskItem()

        task.id = "task1"
        task.superObjectId = "obj1"
        task.columnId = "col1"
        task.title = "Implement feature"
        task.description = "Create login flow"
        task.orderInColumn = 1
        task.assigneeId = 100
        task.reporterId = 200
        task.priority = "HIGH"
        task.dueDate = now
        task.tags = listOf("backend", "urgent")
        task.attachments = listOf(10, 20)
        task.subtasks = listOf("sub1", "sub2")

        assertEquals("task1", task.id)
        assertEquals("obj1", task.superObjectId)
        assertEquals("col1", task.columnId)
        assertEquals("Implement feature", task.title)
        assertEquals("Create login flow", task.description)
        assertEquals(1, task.orderInColumn)
        assertEquals(100, task.assigneeId)
        assertEquals(200, task.reporterId)
        assertEquals("HIGH", task.priority)
        assertEquals(now, task.dueDate)
        assertEquals(listOf("backend", "urgent"), task.tags)
        assertEquals(listOf(10, 20), task.attachments)
        assertEquals(listOf("sub1", "sub2"), task.subtasks)
    }
}
